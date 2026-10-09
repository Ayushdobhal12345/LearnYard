const BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';
const TOKEN_KEY = 'ila_token';
const STUDENT_KEY = 'ila_student';
const SUBJECT_KEY = 'ila_subject';

function loadAuthFromStorage() {
  const token = localStorage.getItem(TOKEN_KEY);
  const studentRaw = localStorage.getItem(STUDENT_KEY);
  if (token && studentRaw) {
    try {
      return { token, student: JSON.parse(studentRaw) };
    } catch {
      return null;
    }
  }
  return null;
}

function loadSubjectFromStorage() {
  const raw = localStorage.getItem(SUBJECT_KEY);
  if (raw) {
    try {
      return JSON.parse(raw);
    } catch {
      return null;
    }
  }
  return null;
}

let currentAuth = loadAuthFromStorage();
let currentSubject = loadSubjectFromStorage();

export function getAuthToken() {
  return currentAuth?.token || null;
}
export function getCurrentStudent() {
  return currentAuth?.student || null;
}
export function isLoggedIn() {
  return !!currentAuth;
}
export function loginSuccess(token, student) {
  currentAuth = { token, student };
  localStorage.setItem(TOKEN_KEY, token);
  localStorage.setItem(STUDENT_KEY, JSON.stringify(student));
}
export function logout() {
  currentAuth = null;
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(STUDENT_KEY);
}

export function getCurrentSubject() {
  return currentSubject;
}
export function getCurrentSubjectId() {
  return currentSubject?.id || null;
}
export function selectSubject(subject) {
  currentSubject = subject;
  localStorage.setItem(SUBJECT_KEY, JSON.stringify(subject));
  window.location.reload();
}
/** Forgets the selected subject, for example when it was deleted or belongs to another account. */
export function clearSubject() {
  currentSubject = null;
  localStorage.removeItem(SUBJECT_KEY);
}

export function getCurrentStudentId() {
  return getCurrentStudent()?.studentId || null;
}

async function request(path, options = {}) {
  const headers = { 'Content-Type': 'application/json' };
  const token = getAuthToken();
  if (token) headers['Authorization'] = `Bearer ${token}`;

  const res = await fetch(`${BASE_URL}${path}`, { headers, ...options });

  if (!res.ok) {
    let message = `Request failed (${res.status})`;
    try {
      const body = await res.json();
      message = body.message || message;
    } catch {
      // not JSON
    }
    if (res.status === 401 && path !== '/auth/login') logout();
    throw new Error(message);
  }

  const contentType = res.headers.get('content-type') || '';
  if (contentType.includes('application/json')) return res.json();
  return res.text();
}

async function uploadFileRequest(path, formData) {
  const headers = {};
  const token = getAuthToken();
  if (token) headers['Authorization'] = `Bearer ${token}`;

  const res = await fetch(`${BASE_URL}${path}`, { method: 'POST', headers, body: formData });

  if (!res.ok) {
    let message = `Upload failed (${res.status})`;
    try {
      const body = await res.json();
      message = body.message || message;
    } catch {
      // not JSON
    }
    throw new Error(message);
  }
  return res.json();
}

export const authApi = {
  register: (name, email, password) =>
    request('/auth/register', { method: 'POST', body: JSON.stringify({ name, email, password }) }),
  login: (email, password) =>
    request('/auth/login', { method: 'POST', body: JSON.stringify({ email, password }) }),
  updateProfile: (name) =>
    request('/auth/profile', { method: 'PUT', body: JSON.stringify({ name }) }),
};

export const api = {
  // Dashboard / mastery / priority / plan / quiz / revision / ai - unchanged
  getDashboard: (studentId) => request(`/analytics/student/${studentId}/dashboard`),
  getMasteryForStudent: (studentId) => request(`/mastery/student/${studentId}`),
  getPriorityRanking: (studentId, subjectId) => request(`/priority/student/${studentId}/ranking?subjectId=${subjectId}`),
  getActivePlan: (studentId) => request(`/study-plans/student/${studentId}/active`),
  getPlanHistory: (studentId) => request(`/study-plans/student/${studentId}/history`),
  generatePlan: (payload) =>
    request(`/study-plans/generate`, { method: 'POST', body: JSON.stringify(payload) }),
  getQuestionsByTopic: (topicId) => request(`/quiz/questions/by-topic/${topicId}`),
  submitQuiz: (payload) =>
    request(`/quiz/submit`, { method: 'POST', body: JSON.stringify(payload) }),
  getDueForRevision: (studentId, subjectId) => request(`/revision/student/${studentId}/due?subjectId=${subjectId}`),
  markRevised: (studentId, topicId) =>
    request(`/revision/student/${studentId}/topic/${topicId}/mark-revised`, { method: 'POST' }),
  sendChatMessage: (studentId, message, subjectId) =>
    request(`/ai/chat`, { method: 'POST', body: JSON.stringify({ studentId, message, subjectId }) }),

  // Subjects
  getSubjects: () => request('/subjects'),
  createSubject: (name, description) =>
    request('/subjects', { method: 'POST', body: JSON.stringify({ name, description }) }),

  // Units
  getUnitsBySubject: (subjectId) => request(`/units/by-subject/${subjectId}`),
  createUnit: (payload) => request('/units', { method: 'POST', body: JSON.stringify(payload) }),

  // Topics
  getTopicsByUnit: (unitId) => request(`/topics/by-unit/${unitId}`),
  createTopic: (payload) => request('/topics', { method: 'POST', body: JSON.stringify(payload) }),
  getAllTopics: () => request('/topics'),

  // What the student has set up so far (units, topics, notes) for the current subject
  getSetupStatus: (subjectId) => request(`/insights/status?subjectId=${subjectId}`),

  // Quiz questions the student has answered wrongly
  getMistakes: (studentId, subjectId) =>
    request(`/insights/mistakes/${studentId}?subjectId=${subjectId}`),

  // Readiness and root-cause insights
  getReadiness: (studentId, subjectId, daysLeft) =>
    request(`/insights/readiness/${studentId}?subjectId=${subjectId}${daysLeft ? `&daysLeft=${daysLeft}` : ''}`),
  getRootCauses: (studentId, subjectId) =>
    request(`/insights/root-causes/${studentId}?subjectId=${subjectId}`),

  // Viva practice
  generateVivaQuestions: (studentId, topicId, count) =>
    request(`/ai/viva/questions?studentId=${studentId}&topicId=${topicId}&count=${count}`, { method: 'POST' }),
  gradeViva: (studentId, topicId, answers) =>
    request('/ai/viva/grade', { method: 'POST', body: JSON.stringify({ studentId, topicId, answers }) }),

  // Study sessions (focus timer)
  logStudySession: (studentId, topicId, durationMinutes) =>
    request('/sessions', { method: 'POST', body: JSON.stringify({ studentId, topicId, durationMinutes }) }),
  getStudySessions: (studentId) => request(`/sessions/student/${studentId}`),
  getTopicsForSubject: async (subjectId) => {
    const units = await request(`/units/by-subject/${subjectId}`);
    const topicLists = await Promise.all(units.map((u) => request(`/topics/by-unit/${u.id}`)));
    return topicLists.flat();
  },

  // Study materials
  uploadMaterial: (file, topicId) => {
    const fd = new FormData();
    fd.append('file', file);
    fd.append('topicId', topicId);
    return uploadFileRequest('/materials', fd);
  },
  getMaterialsByTopic: (topicId) => request(`/materials/by-topic/${topicId}`),
    generateQuizQuestions: (topicId, count = 5) =>
      request(`/quiz/generate?topicId=${topicId}&count=${count}`, { method: 'POST' }),
         importSyllabusForSubject: (file, subjectId) => {
           const fd = new FormData();
           fd.append('file', file);
           fd.append('subjectId', subjectId);
           return uploadFileRequest('/syllabus-import/subject', fd);
         },
         importSyllabusForUnit: (file, unitId) => {
           const fd = new FormData();
           fd.append('file', file);
           fd.append('unitId', unitId);
           return uploadFileRequest('/syllabus-import/unit', fd);
         },
          getMasteryHistory: (studentId, topicId) => request(`/mastery/student/${studentId}/topic/${topicId}/history`),
          getWeeklyInsight: (studentId) => request(`/ai/insight/${studentId}`),
            getPyqPapers: (subjectId) => request(`/pyq/papers/by-subject/${subjectId}`),
            getExamImportance: (topicId, subjectId) => request(`/pyq/exam-importance/${topicId}?subjectId=${subjectId}`),
            importPyqPaper: (file, subjectId, year) => {
              const fd = new FormData();
              fd.append('file', file);
              fd.append('subjectId', subjectId);
              fd.append('year', year);
              return uploadFileRequest('/pyq-import', fd);
            },
              getLesson: (studentId, topicId, refresh = false) => request(`/ai/lesson?studentId=${studentId}&topicId=${topicId}&refresh=${refresh}`),
                deleteSubject: (id) => request(`/subjects/${id}`, { method: 'DELETE' }),
                deleteUnit: (id) => request(`/units/${id}`, { method: 'DELETE' }),
                deleteTopic: (id) => request(`/topics/${id}`, { method: 'DELETE' }),
                  getFlashcards: (topicId, count = 8, refresh = false) => request(`/ai/flashcards?topicId=${topicId}&count=${count}&refresh=${refresh}`),
                    getChatHistory: (studentId) => request(`/ai/chat/history/${studentId}`),
                      clearChatHistory: (studentId) => request(`/ai/chat/history/${studentId}`, { method: 'DELETE' }),
                        getPrerequisitesOf: (topicId) => request(`/dependencies/prerequisites-of/${topicId}`),
                          getActivePlan: (studentId, subjectId) => request(`/study-plans/student/${studentId}/active?subjectId=${subjectId}`),
                          getPlanHistory: (studentId, subjectId) => request(`/study-plans/student/${studentId}/history?subjectId=${subjectId}`),
                            getDependenciesForSubject: (subjectId) => request(`/dependencies/by-subject/${subjectId}`),
                            createDependency: (topicId, prerequisiteTopicId) =>
                              request(`/dependencies`, { method: 'POST', body: JSON.stringify({ topicId, prerequisiteTopicId }) }),
                            deleteDependency: (id) => request(`/dependencies/${id}`, { method: 'DELETE' }),
                              inferDependencies: (subjectId) => request(`/ai/infer-dependencies/${subjectId}`, { method: 'POST' }),
                                uploadMaterialForUnit: (file, unitId) => {
                                  const fd = new FormData();
                                  fd.append('file', file);
                                  fd.append('unitId', unitId);
                                  return uploadFileRequest('/materials/by-unit', fd);
                                },
                                getMaterialsByUnit: (unitId) => request(`/materials/by-unit/${unitId}`),
};