# Business Rules

This page records behavior found in the current service and model code.

## Beach zones

- A zone stores a name, unique analytic account code, and current flag (`GREEN` or `RED`).
- The entity initializes current flag to `GREEN` unless set otherwise.
- Zone create/update/delete are exposed by the zone controller; role permissions are documented separately.

## Water samples and safety classification

- A sample records zone ID, E. coli count, enterococci count, sampled time, and safe status.
- On sample creation, the service looks up the requested zone. Missing zone is rejected.
- Creation classifies a sample unsafe when `ecoliCount > 235` OR `enterococciCount > 104`; otherwise it is safe. Equality at either threshold alone does not exceed that threshold.
- On create, the service overwrites `isSafe` using this classification and sets the zone's current flag to RED for unsafe or GREEN for safe.
- Latest sample is chosen by sampled time descending, with ID descending as a tie-breaker. History is also ordered newest first.
- Status returns the zone's stored flag and latest sample values. If the zone has no samples, latest sample fields are null. Zone monitoring returns a nullable latest sample and history.
- Updating a sample copies the supplied `isSafe` value and measurement/time/zone fields without rerunning threshold classification or updating the zone flag. Deleting a sample does not recalculate the flag. Thus the stored zone flag reflects the last sample creation, not necessarily a recalculation over all records.

## Contacts

- Contact type is one of `VENDOR`, `CUSTOMER`, or `INTERNAL`.
- Contact name and type are required by the entity.
- Ledger entries refer to a contact by ID; ledger create/update verifies the contact exists.

## Ledger and financial summaries

- A ledger entry requires a zone ID, contact ID, amount, and type; description is optional.
- Create/update validates that zone and contact IDs are present and exist.
- Types are `INCOME_PERMIT` and `EXPENSE_LAB`.
- Total income is the sum of `INCOME_PERMIT`; total expense is the sum of `EXPENSE_LAB`; net balance is income minus expense.
- Summaries can be calculated globally or for a zone. A ledger entry has no timestamp.
- Financial report `from` and `to` values are checked for valid order but cannot be applied to entries. When a type is specified, totals are calculated from entries of that type; the other total remains zero.

## Water-quality reports

- Sample reports may filter by zone, sampled-time bounds, and safe status.
- Water-quality statistics report total, safe, and unsafe sample counts plus average E. coli and enterococci counts to two decimal places. Empty result sets return zero counts and zero averages.
- If both `from` and `to` are present and `from` is later, report endpoints reject the request with `400`.
- A supplied unknown zone is rejected rather than treated as an empty match.

## User rules

- Registration trims username, rejects blank username/password and duplicate usernames, hashes the password, assigns VIEWER, and enables the new account.
- Login requires a matching username/password and an enabled user.
- Admin-created user role defaults to VIEWER and enabled defaults to true when omitted.
- User update changes only fields provided; blank/null password does not replace the stored password.
- The currently authenticated user cannot delete their own account.

## Authorization

ADMIN has user-management access and full resource permissions. OPERATOR can read all operational resources and create/update zones, samples, contacts, and ledger entries, but cannot delete those resources. VIEWER has read-only access to these resources. Monitoring, dashboards, and reports are readable by all three roles. The backend enforces access independent of any client UI.
