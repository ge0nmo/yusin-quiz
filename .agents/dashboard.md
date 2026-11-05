# Admin Dashboard

`GET /api/admin/dashboard` is protected by `ROLE_ADMIN` and returns only content counts:

```json
{
  "data": {
    "qualificationExamCount": 1,
    "subjectCount": 5,
    "examCount": 10,
    "problemCount": 400
  }
}
```

Q&A, pending-answer, lecture coverage and user-study metrics were removed.
