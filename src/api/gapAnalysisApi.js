import api from './axios';

export const gapAnalysisApi = {
  getMyReport: () => api.get('/gap-analysis/me'),
  getReportForUser: (userId) => api.get(`/gap-analysis/user/${userId}`),
  getReportsForDepartment: (deptId) => api.get(`/gap-analysis/department/${deptId}`),
  getAllReports: () => api.get('/gap-analysis/all'),
  getDepartmentSummaries: () => api.get('/gap-analysis/department-summary'),
  getTrends: () => api.get('/gap-analysis/trends'),
};
