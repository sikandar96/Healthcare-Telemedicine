# Healthcare Telemedicine Postman assets

Import `Healthcare-Telemedicine.postman_collection.json` and `Healthcare-Telemedicine.postman_environment.json` into Postman. Set `baseUrl`, `username`, and `password`, then run **Authentication > Login** first. The collection stores the returned JWT in the `jwt` collection variable.

The collection covers the dedicated controller routes and the consolidated `/api/healthcare` routes, including doctor consultation, pharmacy delivery, preventive reminders, health programs, revenue, and role management. MongoDB must be running and the logged-in user must have the required database role for protected requests.
