from pathlib import Path
import re

root = Path('/home/ubuntu/Healthcare-Telemedicine/src/main/java/com/health/care')
platform = root / 'platform'
dtos = root / 'dtos'
entities = root / 'entities'
enums = root / 'enums'
for directory in (dtos, entities, enums):
    directory.mkdir(parents=True, exist_ok=True)

entity_source = (platform / 'PlatformEntities.java').read_text()
entity_imports = '''import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import com.health.care.enums.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
'''
starts = [i for i, line in enumerate(entity_source.splitlines()) if line.startswith('@Document') or line.startswith('@Data') or line.startswith('enum ')]
lines = entity_source.splitlines()
for index, start in enumerate(starts):
    end = starts[index + 1] if index + 1 < len(starts) else len(lines)
    block = '\n'.join(lines[start:end]).strip()
    match = re.search(r'\b(class|enum)\s+(\w+)', block)
    if not match:
        continue
    kind, name = match.groups()
    block = block.replace(f'class {name}', f'public class {name}', 1).replace(f'enum {name}', f'public enum {name}', 1)
    target = enums if kind == 'enum' else entities
    imports = '' if kind == 'enum' else entity_imports
    (target / f'{name}.java').write_text(f'package com.health.care.{"enums" if kind == "enum" else "entities"};\n\n{imports}\n{block}\n')

# DTO records are one top-level declaration per line in the source.
dto_source = (platform / 'PlatformDtos.java').read_text()
dto_imports = '''import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import com.health.care.enums.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
'''
for line in dto_source.splitlines():
    line = line.strip()
    if not line.startswith('record '):
        continue
    name = re.match(r'record\s+(\w+)', line).group(1)
    (dtos / f'{name}.java').write_text(f'package com.health.care.dtos;\n\n{dto_imports}\npublic {line}\n')

# Replace package-local type resolution in platform services/controllers/repositories/tests.
for path in list(platform.glob('*.java')) + list((root / 'config').glob('*.java')) + list((root / 'services').glob('*.java')) + list((root / 'rest' / 'controller').glob('*.java')) + list((root / 'security').glob('*.java')) + list((root / 'healthcare').glob('*.java')) + list((root / 'test').glob('*.java')):
    if not path.is_file() or path.name in {'PlatformEntities.java', 'PlatformDtos.java'}:
        continue
    text = path.read_text()
    if 'com.health.care.platform' in text or path.name == 'PlatformServiceTest.java':
        package_line = re.search(r'^package [^;]+;\n', text, re.M)
        if package_line:
            imports = 'import com.health.care.dtos.*;\nimport com.health.care.entities.*;\nimport com.health.care.enums.*;\n'
            if imports not in text:
                text = text[:package_line.end()] + '\n' + imports + text[package_line.end():]
            path.write_text(text)

# Repositories remain in the platform package, but need explicit imports for moved entities/enums.
repo = platform / 'PlatformRepositories.java'
text = repo.read_text()
if 'import com.health.care.entities.*;' not in text:
    text = text.replace('package com.health.care.platform;\n', 'package com.health.care.platform;\n\nimport com.health.care.entities.*;\nimport com.health.care.enums.*;\n')
repo.write_text(text)

(platform / 'PlatformEntities.java').unlink()
(platform / 'PlatformDtos.java').unlink()
