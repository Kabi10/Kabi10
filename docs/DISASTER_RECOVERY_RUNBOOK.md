# Disaster Recovery Runbook

## Sri Lanka Farmers Marketplace - Incident Response & Recovery Procedures

**Version:** 1.0
**Last Updated:** 2024-11-27
**Classification:** Internal Operations

---

## Table of Contents

1. [Overview](#overview)
2. [Contact Information](#contact-information)
3. [Incident Classification](#incident-classification)
4. [Incident Response Procedures](#incident-response-procedures)
5. [Database Backup & Recovery](#database-backup--recovery)
6. [Service Recovery Procedures](#service-recovery-procedures)
7. [Post-Incident Review](#post-incident-review)

---

## Overview

This runbook provides step-by-step procedures for responding to and recovering from service disruptions affecting the Sri Lanka Farmers Marketplace application.

### Infrastructure Components

| Component | Provider | Recovery Priority |
|-----------|----------|-------------------|
| Backend API | Vercel Serverless | P1 - Critical |
| Database | Supabase (PostgreSQL) | P1 - Critical |
| Authentication | Supabase Auth | P1 - Critical |
| Android App | Google Play Store | P2 - High |
| File Storage | Supabase Storage | P2 - High |

---

## Contact Information

### Escalation Contacts

| Role | Primary Contact | Backup Contact |
|------|-----------------|----------------|
| On-Call Engineer | [Configure in PagerDuty] | [Configure] |
| Database Admin | [Configure] | [Configure] |
| Security Lead | [Configure] | [Configure] |

### External Vendor Support

| Service | Support URL | SLA |
|---------|-------------|-----|
| Supabase | https://supabase.com/dashboard/support | 24hr response |
| Vercel | https://vercel.com/support | 24hr response |
| Firebase | https://firebase.google.com/support | 48hr response |

---

## Incident Classification

### Severity Levels

| Level | Description | Response Time | Examples |
|-------|-------------|---------------|----------|
| **SEV-1** | Complete service outage | 15 minutes | Database down, API unresponsive |
| **SEV-2** | Major feature degraded | 1 hour | Auth failures, transactions failing |
| **SEV-3** | Minor feature impact | 4 hours | Slow queries, image upload issues |
| **SEV-4** | Low impact issue | 24 hours | UI glitches, non-critical bugs |

---

## Incident Response Procedures

### 1. Initial Assessment (First 5 Minutes)

```
□ Acknowledge the incident
□ Determine severity level
□ Check service status dashboards:
  - Vercel: https://www.vercel-status.com/
  - Supabase: https://status.supabase.com/
□ Notify stakeholders if SEV-1 or SEV-2
□ Start incident timeline documentation
```

### 2. Diagnosis (5-15 Minutes)

```
□ Check Vercel function logs:
  vercel logs --follow

□ Check Supabase database status:
  - Dashboard: https://supabase.com/dashboard/project/[PROJECT_ID]
  - Check connection pool usage
  - Review slow query log

□ Check Firebase Crashlytics for app crashes
□ Review recent deployments (potential rollback candidates)
```

### 3. Mitigation

#### API Issues
```bash
# Rollback to previous deployment
vercel rollback [deployment-url]

# Or redeploy from last known good commit
git checkout [commit-hash]
vercel --prod
```

#### Database Issues
```sql
-- Check active connections
SELECT count(*) FROM pg_stat_activity WHERE state = 'active';

-- Kill long-running queries (if needed)


### Database Restore Procedure

```bash
# 1. Stop application traffic (optional, for consistency)
# Update Vercel environment to maintenance mode

# 2. Restore from backup file
pg_restore -h [SUPABASE_HOST] -U postgres -d postgres \
  --clean --if-exists \
  backup_file.dump

# 3. Verify data integrity
psql -h [SUPABASE_HOST] -U postgres -d postgres -c "
  SELECT 'users' as table_name, count(*) FROM users
  UNION ALL
  SELECT 'listings', count(*) FROM listings
  UNION ALL
  SELECT 'transactions', count(*) FROM transactions;
"

# 4. Resume application traffic
```

### Point-in-Time Recovery (Supabase Pro)

1. Go to Supabase Dashboard → Project → Database → Backups
2. Select "Point-in-time Recovery"
3. Choose target timestamp (before incident)
4. Confirm restore operation
5. Verify data integrity after restore

---

## Service Recovery Procedures

### Vercel API Recovery

**Complete Outage:**
```bash
# 1. Check Vercel status
curl https://api.vercel.com/v2/status

# 2. Redeploy from main branch
cd backend
vercel --prod --force

# 3. Verify deployment
curl https://[YOUR-APP].vercel.app/api/health
```

**Partial Outage (specific functions):**
```bash
# Identify failing functions from logs
vercel logs --filter="error"

# Redeploy specific function if needed
vercel --prod
```

### Database Connection Issues

```sql
-- Check connection count
SELECT count(*), state FROM pg_stat_activity GROUP BY state;

-- If connection exhaustion:
-- 1. Increase pool size in Supabase dashboard
-- 2. Or implement connection pooling (PgBouncer)

-- Reset stuck connections
SELECT pg_terminate_backend(pid)
FROM pg_stat_activity
WHERE state = 'idle'
AND query_start < now() - interval '1 hour';
```

### Android App Issues

**Critical Bug in Production:**
1. Prepare hotfix in new branch
2. Test on physical devices
3. Build release APK with incremented version
4. Submit to Google Play for expedited review
5. Consider staged rollout (10% → 50% → 100%)

**Rollback to Previous Version:**
1. Go to Google Play Console
2. Select the app → Release → Production
3. Click "Manage" → "Halt rollout"
4. Users retain previous version until update

---

## Post-Incident Review

### Incident Report Template

```markdown
## Incident Report: [TITLE]

**Date:** [DATE]
**Duration:** [START] to [END]
**Severity:** SEV-[1/2/3/4]
**Author:** [NAME]

### Summary
[Brief description of what happened]

### Timeline
- HH:MM - [Event]
- HH:MM - [Event]
- HH:MM - [Resolution]

### Root Cause
[Technical explanation of why the incident occurred]

### Impact
- Users affected: [NUMBER]
- Revenue impact: [ESTIMATE]
- Data loss: [YES/NO, details]

### Resolution
[What was done to resolve the issue]

### Action Items
- [ ] [Preventive measure 1]
- [ ] [Preventive measure 2]
- [ ] [Documentation update]
```

### Review Meeting Agenda

1. Incident timeline review (10 min)
2. Root cause analysis (15 min)
3. What went well (5 min)
4. What could be improved (10 min)
5. Action items and owners (10 min)

---

## Appendix

### Environment Variables Reference

| Variable | Service | Purpose |
|----------|---------|---------|
| `SUPABASE_URL` | Supabase | Database connection |
| `SUPABASE_SERVICE_KEY` | Supabase | Admin operations |
| `JWT_SECRET` | Auth | Token signing |
| `REQUEST_SIGNING_SECRET` | API | Request validation |

### Useful Commands

```bash
# Check Vercel deployment status
vercel ls

# View recent logs
vercel logs --since 1h

# Check database size
psql -c "SELECT pg_size_pretty(pg_database_size('postgres'));"

# Monitor active queries
watch -n 5 'psql -c "SELECT pid, now() - query_start as duration, query FROM pg_stat_activity WHERE state = '\''active'\'';"'
```

### Recovery Checklist

```
□ Incident acknowledged and classified
□ Stakeholders notified
□ Root cause identified
□ Mitigation applied
□ Service restored
□ Data integrity verified
□ Monitoring confirmed normal
□ Incident report drafted
□ Post-incident review scheduled
```

---

*This runbook should be reviewed and updated quarterly or after any significant incident.*