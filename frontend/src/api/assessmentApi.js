import api from './axios';

export const assessmentApi = {
  getQuestions: (skillId) => api.get(`/assessments/questions/${skillId}`),
  submit: (data) => api.post('/assessments/submit', data),
  getMyResults: () => api.get('/assessments/my-results'),
};
