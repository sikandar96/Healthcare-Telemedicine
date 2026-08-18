# Healthcare Telemedicine Postman Assets

Import `healthcare-telemedicine.postman_collection.json` into Postman and select `healthcare-telemedicine.local.postman_environment.json` as the active environment.

Set `baseUrl` to the running backend URL. Run **Register patient** or **Login** first. The collection-level test script stores a successful response's `data.token` in the `access_token` environment variable, which is used by protected requests.

Role-specific requests require a token for the corresponding MongoDB role. Set `access_token` manually when testing doctor, pharmacy-partner, health-manager, or administrator flows. The administrator role must be created or assigned through the application's approved MongoDB bootstrap or administrator role-management flow.

For payment creation, keep a unique `Idempotency-Key` value in `idempotency_key`. Repeating a payment request with the same payer and key should return the original transaction rather than create a duplicate. Use a new key for a new payment.

The collection contains the dedicated controller routes and the consolidated `/api/healthcare/**` routes, plus public health, authentication, platform, notification, consent, campaign, inventory, payment, and audit operations. Replace placeholder IDs such as `doctor_id`, `pharmacy_id`, `consultation_id`, and `payment_id` with IDs returned by earlier requests.

The environment contains example credentials only. Replace them before use and do not commit real passwords or JWTs.
