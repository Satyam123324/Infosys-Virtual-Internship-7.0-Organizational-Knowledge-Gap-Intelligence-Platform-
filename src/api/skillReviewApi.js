import api from './axios';

// 360° skill review module (self / peer / manager assessments).
export const skillReviewApi = {
  submit: (data) => api.post('/skill-reviews', data),
  getReceived: () => api.get('/skill-reviews/received'),
  getSubmitted: () => api.get('/skill-reviews/submitted'),
  getReviewableUsers: () => api.get('/skill-reviews/reviewable-users'),
};
