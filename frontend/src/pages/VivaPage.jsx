import { useState, useEffect, useRef } from 'react';
import { api, getCurrentSubjectId, getCurrentStudentId } from '../api/client';
import PageHeader from '../components/PageHeader';
import Markdown from '../components/Markdown';

const COUNTS = [3, 5, 8];

const card = {
  background: 'var(--surface)',
  border: '1px solid var(--border)',
  borderRadius: 'var(--radius-lg)',
  padding: 32,
};

const primaryBtn = {
  padding: '12px 24px', fontSize: 14, fontWeight: 700, borderRadius: 11,
  border: 'none', background: 'var(--lime)', color: 'var(--dark)', cursor: 'pointer',
};

const ghostBtn = {
  padding: '12px 22px', fontSize: 14, fontWeight: 700, borderRadius: 11,
  border: '1px solid var(--border-strong)', background: '#fff', color: 'var(--dark)', cursor: 'pointer',
};

function scoreColor(score) {
  if (score >= 8) return '#3f7d1f';
  if (score >= 5) return '#b9770e';
  return '#c0392b';
}

function VivaPage() {
  const studentId = getCurrentStudentId();

  const [phase, setPhase] = useState('setup'); // setup | asking | grading | results
  const [topics, setTopics] = useState([]);
  const [topicId, setTopicId] = useState('');
  const [count, setCount] = useState(5);
  const [questions, setQuestions] = useState([]);
  const [answers, setAnswers] = useState([]);
  const [index, setIndex] = useState(0);
  const [current, setCurrent] = useState('');
  const [result, setResult] = useState(null);
  const [starting, setStarting] = useState(false);
  const [listening, setListening] = useState(false);
  const [error, setError] = useState('');

  const recognitionRef = useRef(null);

  const canSpeak = typeof window !== 'undefined' && 'speechSynthesis' in window;
  const canListen =
    typeof window !== 'undefined' && ('SpeechRecognition' in window || 'webkitSpeechRecognition' in window);

  useEffect(() => {
    api.getTopicsForSubject(getCurrentSubjectId())
      .then((t) => {
        setTopics(t);
        if (t.length > 0) setTopicId(String(t[0].id));
      })
      .catch((err) => setError(err.message));
  }, []);

  const stopMic = () => {
    if (recognitionRef.current) recognitionRef.current.stop();
    setListening(false);
  };

  const speak = (text) => {
    if (!canSpeak) return;
    window.speechSynthesis.cancel();
    const utterance = new SpeechSynthesisUtterance(text);
    utterance.lang = 'en-IN';
    window.speechSynthesis.speak(utterance);
  };

  const toggleMic = () => {
    if (listening) {
      stopMic();
      return;
    }
    const Recognition = window.SpeechRecognition || window.webkitSpeechRecognition;
    const recognition = new Recognition();
    recognition.lang = 'en-IN';
    recognition.interimResults = false;
    recognition.continuous = false;
    recognition.onresult = (event) => {
      let heard = '';
      for (let i = event.resultIndex; i < event.results.length; i += 1) {
        heard += event.results[i][0].transcript;
      }
      setCurrent((prev) => (prev ? `${prev} ` : '') + heard.trim());
    };
    recognition.onend = () => setListening(false);
    recognition.onerror = () => setListening(false);
    recognitionRef.current = recognition;
    recognition.start();
    setListening(true);
  };

  const handleStart = () => {
    if (!topicId) {
      setError('Pick a topic first. Add topics under Curriculum if the list is empty.');
      return;
    }
    setError('');
    setStarting(true);
    api.generateVivaQuestions(studentId, Number(topicId), count)
      .then((res) => {
        setQuestions(res.questions);
        setAnswers(res.questions.map(() => ''));
        setIndex(0);
        setCurrent('');
        setResult(null);
        setPhase('asking');
        speak(res.questions[0]);
      })
      .catch((err) => setError(err.message))
      .finally(() => setStarting(false));
  };

  const submit = (finalAnswers) => {
    setError('');
    setPhase('grading');
    const payload = questions.map((q, i) => ({ question: q, answer: finalAnswers[i] || '' }));
    api.gradeViva(studentId, Number(topicId), payload)
      .then((res) => {
        setResult(res);
        setPhase('results');
      })
      .catch((err) => {
        setError(err.message);
        setPhase('asking');
      });
  };

  const goTo = (nextIndex, updatedAnswers) => {
    setAnswers(updatedAnswers);
    setIndex(nextIndex);
    setCurrent(updatedAnswers[nextIndex] || '');
    speak(questions[nextIndex]);
  };

  const handleNext = () => {
    stopMic();
    if (canSpeak) window.speechSynthesis.cancel();
    const updated = [...answers];
    updated[index] = current;
    if (index < questions.length - 1) {
      goTo(index + 1, updated);
    } else {
      setAnswers(updated);
      submit(updated);
    }
  };

  const handlePrevious = () => {
    stopMic();
    if (canSpeak) window.speechSynthesis.cancel();
    const updated = [...answers];
    updated[index] = current;
    goTo(index - 1, updated);
  };

  const reset = () => {
    stopMic();
    if (canSpeak) window.speechSynthesis.cancel();
    setPhase('setup');
    setQuestions([]);
    setAnswers([]);
    setResult(null);
    setError('');
  };

  const selectStyle = {
    padding: '10px 14px', borderRadius: 10, border: '1px solid var(--border-strong)',
    fontSize: 14, fontFamily: 'var(--font)', background: '#fff', minWidth: 260,
  };

  return (
    <div>
      <PageHeader
        eyebrow="Viva"
        title="Viva practice."
        subtitle="An AI examiner asks you questions one by one, then grades every answer and updates your mastery."
      />

      {/* ---------- Setup ---------- */}
      {phase === 'setup' && (
        <div style={{ ...card, maxWidth: 640 }}>
          <label style={{ display: 'block', fontSize: 13, fontWeight: 700, marginBottom: 8 }}>Topic</label>
          <select value={topicId} onChange={(e) => setTopicId(e.target.value)} style={selectStyle}>
            {topics.length === 0 && <option value="">No topics yet</option>}
            {topics.map((t) => <option key={t.id} value={t.id}>{t.name}</option>)}
          </select>

          <label style={{ display: 'block', fontSize: 13, fontWeight: 700, margin: '22px 0 8px' }}>Number of questions</label>
          <div style={{ display: 'flex', gap: 8 }}>
            {COUNTS.map((c) => (
              <button
                key={c}
                onClick={() => setCount(c)}
                style={{
                  padding: '9px 20px', borderRadius: 10, fontSize: 14, fontWeight: 700, cursor: 'pointer',
                  border: '1px solid var(--border-strong)',
                  background: count === c ? 'var(--dark)' : '#fff',
                  color: count === c ? 'var(--lime)' : 'var(--dark)',
                }}
              >
                {c}
              </button>
            ))}
          </div>

          <div style={{ marginTop: 28, display: 'flex', alignItems: 'center', gap: 16, flexWrap: 'wrap' }}>
            <button onClick={handleStart} disabled={starting} style={{ ...primaryBtn, opacity: starting ? 0.6 : 1 }}>
              {starting ? 'Preparing questions…' : 'Start viva'}
            </button>
            <span style={{ fontSize: 12.5, color: 'var(--gray)' }}>Uses 2 AI requests per session.</span>
          </div>
          {error && <p style={{ fontSize: 13, color: '#c0392b', marginTop: 16, marginBottom: 0 }}>{error}</p>}
        </div>
      )}

      {/* ---------- Asking ---------- */}
      {phase === 'asking' && questions.length > 0 && (
        <div style={{ ...card, maxWidth: 760 }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 14 }}>
            <div style={{ fontSize: 12.5, fontWeight: 700, color: 'var(--gray)', letterSpacing: '0.08em', textTransform: 'uppercase' }}>
              Question {index + 1} of {questions.length}
            </div>
            <div style={{ display: 'flex', gap: 6 }}>
              {questions.map((q, i) => (
                <span
                  key={q}
                  style={{
                    width: 26, height: 6, borderRadius: 99,
                    background: i <= index ? 'var(--lime-deep)' : 'var(--border)',
                  }}
                />
              ))}
            </div>
          </div>

          <h2 style={{ fontSize: 24, lineHeight: 1.35, letterSpacing: '-0.01em', margin: '0 0 8px' }}>
            <Markdown text={questions[index]} inline />
          </h2>
          {canSpeak && (
            <button
              onClick={() => speak(questions[index])}
              style={{ background: 'none', border: 'none', padding: 0, fontSize: 13, fontWeight: 600, color: 'var(--gray)', cursor: 'pointer' }}
            >
              Read question aloud
            </button>
          )}

          <textarea
            value={current}
            onChange={(e) => setCurrent(e.target.value)}
            placeholder="Type your answer, or use the microphone to speak it…"
            rows={6}
            style={{
              width: '100%', boxSizing: 'border-box', marginTop: 18, padding: 14, fontSize: 15, lineHeight: 1.55,
              borderRadius: 12, border: '1px solid var(--border-strong)', fontFamily: 'var(--font)', resize: 'vertical',
            }}
          />

          <div style={{ display: 'flex', gap: 10, marginTop: 18, flexWrap: 'wrap', alignItems: 'center' }}>
            {canListen && (
              <button
                onClick={toggleMic}
                style={{
                  ...ghostBtn,
                  background: listening ? 'var(--dark)' : '#fff',
                  color: listening ? 'var(--lime)' : 'var(--dark)',
                }}
              >
                {listening ? 'Listening… tap to stop' : 'Speak answer'}
              </button>
            )}
            {index > 0 && <button onClick={handlePrevious} style={ghostBtn}>Previous</button>}
            <button onClick={handleNext} style={{ ...primaryBtn, marginLeft: 'auto' }}>
              {index < questions.length - 1 ? 'Next question' : 'Finish and grade'}
            </button>
          </div>
          {error && <p style={{ fontSize: 13, color: '#c0392b', marginTop: 16, marginBottom: 0 }}>{error}</p>}
        </div>
      )}

      {/* ---------- Grading ---------- */}
      {phase === 'grading' && (
        <div style={{ ...card, maxWidth: 560, textAlign: 'center' }}>
          <div style={{ fontSize: 20, fontWeight: 800, marginBottom: 6 }}>Your examiner is marking your answers…</div>
          <div style={{ fontSize: 14, color: 'var(--gray)' }}>This usually takes a few seconds.</div>
        </div>
      )}

      {/* ---------- Results ---------- */}
      {phase === 'results' && result && (
        <div style={{ display: 'grid', gap: 18, maxWidth: 860 }}>
          <div style={{ ...card, display: 'flex', gap: 32, alignItems: 'center', flexWrap: 'wrap' }}>
            <div>
              <div className="tabular" style={{ fontSize: 64, fontWeight: 800, letterSpacing: '-0.03em', lineHeight: 1 }}>
                {result.percentage}%
              </div>
              <div style={{ fontSize: 13, color: 'var(--gray)', marginTop: 6 }}>
                {result.totalScore} of {result.maxScore} marks
              </div>
            </div>
            <div style={{ flex: 1, minWidth: 240 }}>
              <div style={{ fontSize: 15, lineHeight: 1.6 }}><Markdown text={result.summary} inline /></div>
              <div style={{ fontSize: 13, color: 'var(--gray)', marginTop: 10 }}>
                Topic mastery: {result.masteryBefore}% → <b style={{ color: 'var(--dark)' }}>{result.masteryAfter}%</b>
              </div>
            </div>
          </div>

          {result.results.map((r, i) => (
            <div key={r.question} style={{ ...card, padding: '24px 28px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', gap: 16 }}>
                <div style={{ fontSize: 12, fontWeight: 700, color: 'var(--gray)', letterSpacing: '0.08em', textTransform: 'uppercase' }}>
                  Question {i + 1}
                </div>
                <div className="tabular" style={{ fontSize: 20, fontWeight: 800, color: scoreColor(r.score) }}>
                  {r.score}/10
                </div>
              </div>
              <div style={{ fontSize: 17, fontWeight: 700, margin: '8px 0 14px', lineHeight: 1.4 }}><Markdown text={r.question} inline /></div>
              <div style={{ fontSize: 13, fontWeight: 700, color: 'var(--gray)', marginBottom: 4 }}>Your answer</div>
              <div style={{ fontSize: 14.5, lineHeight: 1.6, marginBottom: 14, color: r.answer ? 'inherit' : 'var(--gray)' }}>
                {r.answer || 'No answer given.'}
              </div>
              <div style={{ fontSize: 14.5, lineHeight: 1.6, marginBottom: 14 }}><Markdown text={r.feedback} inline /></div>
              <div style={{ background: 'var(--bg)', borderRadius: 12, padding: '12px 16px' }}>
                <div style={{ fontSize: 12, fontWeight: 700, color: 'var(--gray)', marginBottom: 4 }}>Model answer</div>
                <div style={{ fontSize: 14, lineHeight: 1.6 }}><Markdown text={r.modelAnswer} inline /></div>
              </div>
            </div>
          ))}

          <div style={{ display: 'flex', gap: 10 }}>
            <button onClick={handleStart} disabled={starting} style={{ ...primaryBtn, opacity: starting ? 0.6 : 1 }}>
              {starting ? 'Preparing questions…' : 'Try again on this topic'}
            </button>
            <button onClick={reset} style={ghostBtn}>Choose another topic</button>
          </div>
          {error && <p style={{ fontSize: 13, color: '#c0392b', margin: 0 }}>{error}</p>}
        </div>
      )}
    </div>
  );
}

export default VivaPage;
