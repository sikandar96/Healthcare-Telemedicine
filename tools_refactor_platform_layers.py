from pathlib import Path
import re

root = Path('/home/ubuntu/Healthcare-Telemedicine/src/main/java/com/health/care')
platform = root / 'platform'
repositories = root / 'repositories'
services = root / 'services'
controllers = root / 'rest' / 'controller'
repositories.mkdir(parents=True, exist_ok=True)
services.mkdir(parents=True, exist_ok=True)
controllers.mkdir(parents=True, exist_ok=True)

repo_source = (platform / 'PlatformRepositories.java').read_text()
repo_imports = '''import com.health.care.entities.*;
import com.health.care.enums.*;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
'''
lines = repo_source.splitlines()
starts = [i for i, line in enumerate(lines) if line.startswith('interface ')]
for index, start in enumerate(starts):
    end = starts[index + 1] if index + 1 < len(starts) else len(lines)
    block = '\n'.join(lines[start:end]).strip()
    name = re.search(r'interface\s+(\w+)', block).group(1)
    block = block.replace(f'interface {name}', f'public interface {name}', 1)
    (repositories / f'{name}.java').write_text(f'package com.health.care.repositories;\n\n{repo_imports}\n{block}\n')

service_source = (platform / 'PlatformService.java').read_text()
service_source = re.sub(r'^package\s+com\.health\.care\.platform;', 'package com.health.care.services;', service_source, count=1, flags=re.M)
imports = 'import com.health.care.dtos.*;\nimport com.health.care.entities.*;\nimport com.health.care.enums.*;\nimport com.health.care.repositories.*;\n'
service_source = service_source.replace('package com.health.care.services;\n', 'package com.health.care.services;\n\n' + imports, 1)
(services / 'PlatformService.java').write_text(service_source)

controller_source = (platform / 'PlatformController.java').read_text()
controller_source = re.sub(r'^package\s+com\.health\.care\.platform;', 'package com.health.care.rest.controller;', controller_source, count=1, flags=re.M)
imports = 'import com.health.care.dtos.*;\nimport com.health.care.entities.*;\nimport com.health.care.enums.*;\nimport com.health.care.services.PlatformService;\n'
controller_source = controller_source.replace('package com.health.care.rest.controller;\n', 'package com.health.care.rest.controller;\n\n' + imports, 1)
(controllers / 'PlatformController.java').write_text(controller_source)

# Update the platform regression test package and imports.
test = Path('/home/ubuntu/Healthcare-Telemedicine/src/test/java/com/health/care/platform/PlatformServiceTest.java')
text = test.read_text()
text = text.replace('package com.health.care.platform;', 'package com.health.care.platform;\n\nimport com.health.care.dtos.*;\nimport com.health.care.entities.*;\nimport com.health.care.repositories.*;\nimport com.health.care.services.PlatformService;', 1)
test.write_text(text)

for old in (platform / 'PlatformRepositories.java', platform / 'PlatformService.java', platform / 'PlatformController.java'):
    old.unlink()
