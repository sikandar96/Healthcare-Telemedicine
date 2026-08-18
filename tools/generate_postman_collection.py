import json
from pathlib import Path

ROOT = Path('/home/ubuntu/Healthcare-Telemedicine')


def raw_body(value):
    return {
        'mode': 'raw',
        'raw': json.dumps(value, indent=2),
        'options': {'raw': {'language': 'json'}},
    }


def request(name, method, path, body=None, headers=None, auth=True, description=''):
    item = {
        'name': name,
        'request': {
            'method': method,
            'header': headers or [],
            'url': {'raw': '{{baseUrl}}' + path, 'host': ['{{baseUrl}}'], 'path': path.strip('/').split('/') if path.strip('/') else []},
            'description': description,
        },
    }
    if auth:
        item['request']['auth'] = {'type': 'bearer', 'bearer': [{'key': 'token', 'value': '{{access_token}}', 'type': 'string'}]}
    if body is not None:
        item['request']['body'] = raw_body(body)
        item['request']['header'].append({'key': 'Content-Type', 'value': 'application/json', 'type': 'text'})
    return item


def folder(name, items):
    return {'name': name, 'item': items}


patient_body = {'username': '{{patient_username}}', 'password': '{{patient_password}}'}

items = [
    folder('Authentication', [
        request('Register patient', 'POST', '/api/auth/register', patient_body, auth=False,
                description='Public registration. The application assigns ROLE_PATIENT; the request role field is not trusted.'),
        request('Login', 'POST', '/api/auth/login', patient_body, auth=False),
        request('Current user', 'GET', '/api/auth/me'),
        request('Update user roles (ADMIN)', 'PUT', '/api/auth/users/{{target_username}}/roles', {'roles': ['PATIENT']},
                description='Requires an administrator token. Roles are persisted in MongoDB.'),
    ]),
    folder('Smoke and public endpoints', [
        request('Hello', 'GET', '/api/hello'),
        request('Actuator health', 'GET', '/actuator/health', auth=False),
        request('OpenAPI JSON', 'GET', '/v3/api-docs', auth=False),
        request('Published health programs', 'GET', '/api/healthcare/health-programs', auth=False),
        request('Active platform campaigns', 'GET', '/api/platform/campaigns/active', auth=False),
    ]),
    folder('Dedicated doctor and consultation APIs', [
        request('Register doctor', 'POST', '/api/doctors/register', {
            'username': '{{doctor_username}}', 'name': 'Dr. Jane Doe', 'specialization': 'Cardiology',
            'licenseNumber': 'LIC-1001', 'consultationFee': 500.00, 'bio': 'Certified cardiologist'
        }),
        request('Available doctors', 'GET', '/api/doctors/available'),
        request('Book consultation', 'POST', '/api/consultations/book', {
            'doctorId': '{{doctor_id}}', 'type': 'VIDEO', 'scheduledAt': '2030-01-01T10:00:00Z'
        }),
        request('My consultations', 'GET', '/api/consultations/my'),
        request('Update consultation status', 'PATCH', '/api/consultations/{{consultation_id}}/status', {'status': 'CONFIRMED'}),
    ]),
    folder('Dedicated pharmacy and reminder APIs', [
        request('Add pharmacy', 'POST', '/api/pharmacies/add', {
            'name': 'Local Health Pharmacy', 'address': '100 Main Street', 'phone': '+15550001111', 'commissionRate': 10.00
        }),
        request('Available pharmacies', 'GET', '/api/pharmacies/available'),
        request('Place medicine order', 'POST', '/api/pharmacies/medicine-orders', {
            'pharmacyId': '{{pharmacy_id}}',
            'items': [{'medicineName': 'Paracetamol', 'quantity': 2, 'unitPrice': 25.00}],
            'deliveryAddress': '100 Patient Street'
        }),
        request('My medicine orders', 'GET', '/api/pharmacies/medicine-orders'),
        request('Create reminder', 'POST', '/api/reminders/create', {
            'type': 'VACCINATION', 'title': 'Annual vaccination', 'details': 'Schedule annual vaccination', 'dueDate': '2030-02-01'
        }),
        request('List reminders', 'GET', '/api/reminders/list'),
        request('Complete reminder', 'PATCH', '/api/reminders/{{reminder_id}}/complete'),
    ]),
    folder('Consolidated healthcare APIs', [
        request('Register doctor (consolidated)', 'POST', '/api/healthcare/doctors', {
            'username': '{{doctor_username}}', 'name': 'Dr. Jane Doe', 'specialization': 'Cardiology',
            'licenseNumber': 'LIC-1001', 'consultationFee': 500.00, 'bio': 'Certified cardiologist'
        }),
        request('Available doctors (consolidated)', 'GET', '/api/healthcare/doctors'),
        request('Book consultation (consolidated)', 'POST', '/api/healthcare/consultations', {
            'doctorId': '{{doctor_id}}', 'type': 'VIDEO', 'scheduledAt': '2030-01-01T10:00:00Z'
        }),
        request('My consultations (consolidated)', 'GET', '/api/healthcare/consultations'),
        request('Update consultation (consolidated)', 'PATCH', '/api/healthcare/consultations/{{consultation_id}}/status', {'status': 'CONFIRMED'}),
        request('Add pharmacy (consolidated)', 'POST', '/api/healthcare/pharmacies', {
            'name': 'Local Health Pharmacy', 'address': '100 Main Street', 'phone': '+15550001111', 'commissionRate': 10.00
        }),
        request('Available pharmacies (consolidated)', 'GET', '/api/healthcare/pharmacies'),
        request('Place medicine order (consolidated)', 'POST', '/api/healthcare/medicine-orders', {
            'pharmacyId': '{{pharmacy_id}}', 'items': [{'medicineName': 'Paracetamol', 'quantity': 2, 'unitPrice': 25.00}],
            'deliveryAddress': '100 Patient Street'
        }),
        request('My medicine orders (consolidated)', 'GET', '/api/healthcare/medicine-orders'),
        request('Publish health program', 'POST', '/api/healthcare/health-programs', {
            'title': 'Vaccination Awareness', 'category': 'PREVENTIVE_CARE', 'content': 'Vaccination awareness content',
            'sponsorName': 'Health Foundation', 'sponsored': True, 'sponsorshipFee': 1000.00
        }),
        request('Create reminder (consolidated)', 'POST', '/api/healthcare/reminders', {
            'type': 'VACCINATION', 'title': 'Annual vaccination', 'details': 'Schedule annual vaccination', 'dueDate': '2030-02-01'
        }),
        request('List reminders (consolidated)', 'GET', '/api/healthcare/reminders'),
        request('Complete reminder (consolidated)', 'PATCH', '/api/healthcare/reminders/{{reminder_id}}/complete'),
        request('Revenue summary', 'GET', '/api/healthcare/revenue/summary'),
    ]),
    folder('Platform: verification and appointments', [
        request('Submit doctor verification', 'POST', '/api/platform/doctor-verifications', {
            'username': '{{doctor_username}}', 'licenseNumber': 'LIC-1001'
        }),
        request('Pending doctor verifications', 'GET', '/api/platform/doctor-verifications/pending'),
        request('Decide doctor verification', 'PATCH', '/api/platform/doctor-verifications/{{verification_id}}', {
            'status': 'APPROVED', 'rejectionReason': None
        }),
        request('Book appointment', 'POST', '/api/platform/appointments', {
            'doctorId': '{{doctor_id}}', 'startAt': '2030-01-01T11:00:00', 'endAt': '2030-01-01T11:30:00'
        }),
        request('My appointments', 'GET', '/api/platform/appointments/mine'),
        request('My doctor appointments', 'GET', '/api/platform/appointments/doctor/mine'),
        request('Appointments for doctor (ADMIN)', 'GET', '/api/platform/appointments/doctor/{{doctor_id}}'),
        request('Update appointment status', 'PATCH', '/api/platform/appointments/{{appointment_id}}/status', {'status': 'CONFIRMED'}),
    ]),
    folder('Platform: clinical records and prescriptions', [
        request('Create clinical record', 'POST', '/api/platform/clinical-records', {
            'patientUsername': '{{patient_username}}', 'consultationId': '{{consultation_id}}',
            'diagnosis': 'Routine follow-up', 'notes': 'Patient advised to continue treatment',
            'attachmentUrls': [], 'patientConsent': True
        }),
        request('My clinical records', 'GET', '/api/platform/clinical-records/mine'),
        request('Create prescription', 'POST', '/api/platform/prescriptions', {
            'patientUsername': '{{patient_username}}', 'consultationId': '{{consultation_id}}',
            'items': [{'medicineName': 'Paracetamol', 'dosage': '500 mg', 'frequency': 'Twice daily', 'durationDays': 5}],
            'instructions': 'Take after meals'
        }),
        request('My prescriptions', 'GET', '/api/platform/prescriptions/mine'),
    ]),
    folder('Platform: pharmacy inventory', [
        request('Create or update inventory', 'POST', '/api/platform/inventory', {
            'pharmacyId': '{{pharmacy_id}}', 'medicineName': 'Paracetamol', 'sku': 'PARA-500',
            'quantity': 100, 'unitPrice': 25.00, 'prescriptionRequired': False
        }),
        request('Adjust inventory', 'PATCH', '/api/platform/inventory/{{inventory_id}}', {'quantity': 90}),
        request('List pharmacy inventory', 'GET', '/api/platform/inventory/{{pharmacy_id}}'),
    ]),
    folder('Platform: payments and idempotency', [
        request('Create payment', 'POST', '/api/platform/payments', {
            'referenceType': 'CONSULTATION', 'referenceId': '{{consultation_id}}', 'amount': 500.00, 'currency': 'USD'
        }, headers=[{'key': 'Idempotency-Key', 'value': '{{idempotency_key}}', 'type': 'text'}],
        description='Requires a unique payer-scoped Idempotency-Key. Retrying with the same key returns the original payment.'),
        request('List my payments', 'GET', '/api/platform/payments/mine'),
        request('Update payment status', 'PATCH', '/api/platform/payments/{{payment_id}}', {
            'status': 'PAID', 'providerReference': 'provider-reference-001'
        }),
    ]),
    folder('Platform: notifications and consent', [
        request('List notifications', 'GET', '/api/platform/notifications'),
        request('Mark notification read', 'PATCH', '/api/platform/notifications/{{notification_id}}/read'),
        request('Grant consent', 'POST', '/api/platform/consents', {'grantedTo': '{{doctor_username}}', 'purpose': 'Clinical care'}),
        request('List active consents', 'GET', '/api/platform/consents'),
        request('Revoke consent', 'PATCH', '/api/platform/consents/{{consent_id}}/revoke'),
    ]),
    folder('Platform: campaigns and audit', [
        request('Create campaign', 'POST', '/api/platform/campaigns', {
            'sponsor': 'Health Foundation', 'title': 'Heart Health Month', 'description': 'Preventive heart-health program',
            'budget': 5000.00, 'startDate': '2030-01-01', 'endDate': '2030-01-31'
        }),
        request('Active campaigns', 'GET', '/api/platform/campaigns/active', auth=False),
        request('Audit action count (ADMIN)', 'GET', '/api/platform/audit/count/PAYMENT_CREATED'),
    ]),
]

collection = {
    'info': {
        '_postman_id': 'healthcare-telemedicine-comprehensive-v1',
        'name': 'Healthcare Telemedicine API - Comprehensive',
        'description': 'Import-ready collection for the Spring Boot healthcare and telemedicine backend. Run Login or Register patient first; the response script stores data.token in access_token. Role-specific requests require a token for the appropriate persisted MongoDB role. Payment creation requires Idempotency-Key.',
        'schema': 'https://schema.getpostman.com/json/collection/v2.1.0/collection.json',
    },
    'event': [{
        'listen': 'test',
        'script': {'type': 'text/javascript', 'exec': [
            "if (pm.response.code >= 200 && pm.response.code < 300) {",
            "    try {",
            "        const json = pm.response.json();",
            "        if (json.data && json.data.token) pm.environment.set('access_token', json.data.token);",
            "        if (json.data && json.data.id) pm.environment.set('last_id', json.data.id);",
            "    } catch (e) {}",
            "}",
        ]},
    }],
    'variable': [
        {'key': 'baseUrl', 'value': 'http://localhost:8080'},
        {'key': 'access_token', 'value': ''},
        {'key': 'patient_username', 'value': 'patient@example.com'},
        {'key': 'patient_password', 'value': 'ChangeMe@123'},
        {'key': 'doctor_username', 'value': 'doctor@example.com'},
        {'key': 'doctor_id', 'value': ''},
        {'key': 'pharmacy_id', 'value': ''},
        {'key': 'consultation_id', 'value': ''},
        {'key': 'appointment_id', 'value': ''},
        {'key': 'verification_id', 'value': ''},
        {'key': 'inventory_id', 'value': ''},
        {'key': 'payment_id', 'value': ''},
        {'key': 'notification_id', 'value': ''},
        {'key': 'consent_id', 'value': ''},
        {'key': 'reminder_id', 'value': ''},
        {'key': 'target_username', 'value': 'patient@example.com'},
        {'key': 'idempotency_key', 'value': 'payment-request-001'},
    ],
    'item': items,
}

environment = {
    'name': 'Healthcare Telemedicine - Local',
    'values': [
        {'key': 'baseUrl', 'value': 'http://localhost:8080', 'enabled': True},
        {'key': 'access_token', 'value': '', 'enabled': True},
        {'key': 'patient_username', 'value': 'patient@example.com', 'enabled': True},
        {'key': 'patient_password', 'value': 'ChangeMe@123', 'enabled': True},
        {'key': 'doctor_username', 'value': 'doctor@example.com', 'enabled': True},
        {'key': 'doctor_id', 'value': '', 'enabled': True},
        {'key': 'pharmacy_id', 'value': '', 'enabled': True},
        {'key': 'consultation_id', 'value': '', 'enabled': True},
        {'key': 'appointment_id', 'value': '', 'enabled': True},
        {'key': 'verification_id', 'value': '', 'enabled': True},
        {'key': 'inventory_id', 'value': '', 'enabled': True},
        {'key': 'payment_id', 'value': '', 'enabled': True},
        {'key': 'notification_id', 'value': '', 'enabled': True},
        {'key': 'consent_id', 'value': '', 'enabled': True},
        {'key': 'reminder_id', 'value': '', 'enabled': True},
        {'key': 'target_username', 'value': 'patient@example.com', 'enabled': True},
        {'key': 'idempotency_key', 'value': 'payment-request-001', 'enabled': True},
    ],
    '_postman_variable_scope': 'environment',
    '_postman_exported_using': 'Manus AI',
}

out = ROOT / 'postman'
out.mkdir(exist_ok=True)
(out / 'healthcare-telemedicine.postman_collection.json').write_text(json.dumps(collection, indent=2) + '\n')
(out / 'healthcare-telemedicine.local.postman_environment.json').write_text(json.dumps(environment, indent=2) + '\n')
print(f'Generated {out / "healthcare-telemedicine.postman_collection.json"}')
print(f'Generated {out / "healthcare-telemedicine.local.postman_environment.json"}')
print(f'Request count: {sum(len(x["item"]) for x in items)}')
