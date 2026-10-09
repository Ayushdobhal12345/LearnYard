import { useState, useEffect } from 'react';
import { api, isLoggedIn, getCurrentSubjectId, clearSubject } from './api/client';
import AuthPage from './pages/AuthPage';
import LandingPage from './pages/LandingPage';
import FocusPage from './pages/FocusPage';
import VivaPage from './pages/VivaPage';
import ReadinessPage from './pages/ReadinessPage';
import MistakesPage from './pages/MistakesPage';
import SubjectsPage from './pages/SubjectsPage';
import MaterialsPage from './pages/MaterialsPage';
import Sidebar from './components/Sidebar';
import SetupGate from './components/SetupGate';
import useSetupStatus from './hooks/useSetupStatus';
import GlobalSearch from './components/GlobalSearch';
import DashboardPage from './pages/DashboardPage';
import SyllabusPage from './pages/SyllabusPage';
import PriorityPage from './pages/PriorityPage';
import StudyPlanPage from './pages/StudyPlanPage';
import QuizPage from './pages/QuizPage';
import RevisionPage from './pages/RevisionPage';
import AssistantPage from './pages/AssistantPage';
import SettingsPage from './pages/SettingsPage';
import LearnPage from './pages/LearnPage';
import PyqPage from './pages/PyqPage';
import GraphPage from './pages/GraphPage';
import FlashcardsPage from './pages/FlashcardsPage';

function App() {
  const [loggedIn, setLoggedIn] = useState(isLoggedIn());
  const [activeTab, setActiveTab] = useState('dashboard');
  const [authView, setAuthView] = useState(null); // null = landing page, 'login' | 'register' = auth form
  const [checkedSubjectId, setCheckedSubjectId] = useState(null);

  // The remembered subject must belong to the signed-in student. If it does not (deleted elsewhere,
  // or left over from another account), forget it. The student still lands on the dashboard, and the
  // features that need a curriculum simply ask them to add one.
  const rememberedSubjectId = loggedIn ? getCurrentSubjectId() : null;
  const needsSubjectCheck = rememberedSubjectId !== null && checkedSubjectId !== rememberedSubjectId;

  useEffect(() => {
    if (!needsSubjectCheck) return;
    api.getSubjects()
      .then((list) => {
        if (!list.some((s) => s.id === rememberedSubjectId)) clearSubject();
      })
      .catch(() => {
        // could not check (offline?): keep the selection rather than block the app
      })
      .finally(() => setCheckedSubjectId(rememberedSubjectId));
  }, [needsSubjectCheck, rememberedSubjectId]);

  const setup = useSetupStatus(activeTab, loggedIn && !needsSubjectCheck);

  // Wraps a page so the student is guided to add a curriculum or notes first, instead of seeing errors.
  const gated = (need, feature, page) => (
    <SetupGate need={need} feature={feature} status={setup} onNavigate={setActiveTab}>
      {page}
    </SetupGate>
  );

  if (!loggedIn) {
    if (!authView) {
      return <LandingPage onLogin={() => setAuthView('login')} onSignup={() => setAuthView('register')} />;
    }
    return (
      <AuthPage
        key={authView}
        initialMode={authView}
        onBack={() => setAuthView(null)}
        onSuccess={() => setLoggedIn(true)}
      />
    );
  }

  if (needsSubjectCheck) {
    return <p style={{ padding: '48px 56px', color: 'var(--gray)', fontSize: 14 }}>Loading…</p>;
  }

  return (
    <div style={{ display: 'flex', width: '100%', minHeight: '100vh' }}>
      <Sidebar activeTab={activeTab} onTabChange={setActiveTab} />
      <main style={{ flex: 1, minWidth: 0 }}>
        <div style={{ display: 'flex', justifyContent: 'flex-end', padding: '20px 40px 0' }}>
          <GlobalSearch onNavigate={setActiveTab} />
        </div>
        <div style={{ padding: '20px 40px 48px' }}>
          {activeTab === 'dashboard' && <DashboardPage />}
          {activeTab === 'syllabus' && gated('curriculum', 'Mastery', <SyllabusPage />)}
          {activeTab === 'priority' && gated('curriculum', 'Priority', <PriorityPage />)}
          {activeTab === 'studyplan' && gated('curriculum', 'Study plan', <StudyPlanPage />)}
          {activeTab === 'quiz' && gated('curriculum', 'Quiz', <QuizPage />)}
          {activeTab === 'viva' && gated('curriculum', 'Viva practice', <VivaPage />)}
          {activeTab === 'readiness' && gated('curriculum', 'Readiness', <ReadinessPage />)}
          {activeTab === 'mistakes' && gated('curriculum', 'Mistake notebook', <MistakesPage />)}
          {activeTab === 'revision' && gated('curriculum', 'Revision', <RevisionPage />)}
          {activeTab === 'assistant' && <AssistantPage />}
          {activeTab === 'curriculum' && <SubjectsPage />}
          {activeTab === 'materials' && gated('curriculum', 'Materials', <MaterialsPage />)}
          {activeTab === 'settings' && <SettingsPage />}
          {activeTab === 'learn' && gated('notes', 'Learn', <LearnPage />)}
          {activeTab === 'pyq' && gated('curriculum', 'PYQ', <PyqPage />)}
          {activeTab === 'graph' && gated('curriculum', 'Dependency map', <GraphPage />)}
          {activeTab === 'flashcards' && gated('notes', 'Flashcards', <FlashcardsPage />)}
          {activeTab === 'focus' && gated('curriculum', 'Focus timer', <FocusPage />)}
        </div>
      </main>
    </div>
  );
}

export default App;