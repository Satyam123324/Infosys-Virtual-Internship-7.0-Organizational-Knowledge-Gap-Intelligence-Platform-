import { describe, it, expect } from 'vitest';
import { getCertificationStatus, summarizeCertifications } from './certificationStatus';

const daysFromNow = (n) => {
  const d = new Date();
  d.setDate(d.getDate() + n);
  return d.toISOString();
};

describe('getCertificationStatus', () => {
  it('returns NO_EXPIRY when there is no expiry date', () => {
    expect(getCertificationStatus({ expiryDate: null }).key).toBe('NO_EXPIRY');
  });

  it('flags an already-expired certification', () => {
    expect(getCertificationStatus({ expiryDate: daysFromNow(-5) }).key).toBe('EXPIRED');
  });

  it('flags a certification expiring within 30 days as EXPIRING_SOON', () => {
    expect(getCertificationStatus({ expiryDate: daysFromNow(10) }).key).toBe('EXPIRING_SOON');
  });

  it('marks a far-future certification as VALID', () => {
    expect(getCertificationStatus({ expiryDate: daysFromNow(200) }).key).toBe('VALID');
  });
});

describe('summarizeCertifications', () => {
  it('counts expired and expiring-soon certifications', () => {
    const summary = summarizeCertifications([
      { expiryDate: daysFromNow(-5) },   // expired
      { expiryDate: daysFromNow(5) },    // expiring soon
      { expiryDate: daysFromNow(300) },  // valid
    ]);
    expect(summary.expired).toBe(1);
    expect(summary.expiringSoon).toBe(1);
  });
});
