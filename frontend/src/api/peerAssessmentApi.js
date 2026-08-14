import api from './axios';

export const peerAssessmentApi = {
  submit: (data) => api.post('/peer-assessments', data),
  getReceived: () => api.get('/peer-assessments/received'),
  getGiven: () => api.get('/peer-assessments/given'),
  getColleagues: () => api.get('/employee-profile/colleagues'),
};
