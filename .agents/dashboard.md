# Admin Dashboard

`GET /api/admin/dashboard` is protected by `ROLE_ADMIN` and returns content counts plus the open report count:

```json
{
  "data": {
    "qualificationExamCount": 1,
    "subjectCount": 5,
    "examCount": 10,
    "problemCount": 400,
    "openReportCount": 8
  }
}
```

`openReportCount` counts `RECEIVED` + `IN_REVIEW` reports across every qualification.

Q&A, pending-answer, lecture coverage and user-study metrics were removed.
