import api from './axios';

// Report endpoints return raw file bytes; fetch as a blob and trigger a
// browser download. The backend exposes Content-Disposition (see SecurityConfig).
const download = async (url, fallbackName) => {
  const res = await api.get(url, { responseType: 'blob' });

  let filename = fallbackName;
  const disposition = res.headers['content-disposition'];
  if (disposition) {
    const match = /filename="?([^"]+)"?/.exec(disposition);
    if (match) filename = match[1];
  }

  const blobUrl = window.URL.createObjectURL(new Blob([res.data]));
  const link = document.createElement('a');
  link.href = blobUrl;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  link.remove();
  window.URL.revokeObjectURL(blobUrl);
};

export const reportsApi = {
  myGapPdf: () => download('/reports/gap/me/pdf', 'my-gap-report.pdf'),
  departmentSummaryExcel: () =>
    download('/reports/gap/department-summary/excel', 'department-gap-summary.xlsx'),
  workforceExcel: () => download('/reports/gap/workforce/excel', 'workforce-gap-report.xlsx'),
  trainingEffectivenessExcel: () =>
    download('/reports/training-effectiveness/excel', 'training-effectiveness.xlsx'),
  strategicWorkforcePlanExcel: () =>
    download('/reports/strategic-workforce-plan/excel', 'strategic-workforce-plan.xlsx'),
};
