import { useState, useEffect } from 'react';
import { api, getCurrentStudentId, getCurrentSubjectId } from '../api/client';
import StatusMessage from '../components/StatusMessage';
import PageHeader from '../components/PageHeader';
import Markdown from '../components/Markdown';
import { useToast } from '../context/ToastContext';

function QuizPage() {
  const showToast = useToast();

  const [topics, setTopics] = useState([]);
  const [selectedTopicId, setSelectedTopicId] = useState('');
  const [questions, setQuestions] = useState([]);
  const [generating, setGenerating] = useState(false);
  const [answers, setAnswers] = useState({});
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [result, setResult] = useState(null);

  useEffect(() => {
    api.getTopicsForSubject(getCurrentSubjectId())
      .then((data) => {
        setTopics(data);

        if (data.length > 0) {
          setSelectedTopicId(data[0].id);
        }
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    if (!selectedTopicId) return;

    setAnswers({});
    setResult(null);
    setError('');

    api.getQuestionsByTopic(selectedTopicId)
      .then(setQuestions)
      .catch((err) => setError(err.message));
  }, [selectedTopicId]);

  const handleSelect = (questionId, option) => {
    setAnswers((prev) => ({
      ...prev,
      [questionId]: option
    }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();

    const unanswered = questions.filter((q) => !answers[q.id]);

    if (unanswered.length > 0) {
      setError('Answer every question before submitting.');
      return;
    }

    setError('');
    setSubmitting(true);

    const payload = {
      studentId: getCurrentStudentId(),
      answers: questions.map((q) => ({
        questionId: q.id,
        selectedOption: answers[q.id]
      }))
    };

    api.submitQuiz(payload)
      .then((res) => {
        showToast(
          res.replan.planRegenerated
            ? 'Study plan updated!'
            : 'Quiz submitted!'
        );
        setResult(res);
      })
      .catch((err) => setError(err.message))
      .finally(() => setSubmitting(false));
  };

  if (loading) {
    return <StatusMessage loading={loading} />;
  }

  return (
    <div>
      <PageHeader
        eyebrow="Assessment"
        title="Test what you know."
        subtitle="Every answer updates mastery, and may reshape your plan."
      />

      <div style={{ marginBottom: 28 }}>
        <select
          value={selectedTopicId}
          onChange={(e) => setSelectedTopicId(e.target.value)}
          style={{
            padding: '12px 18px',
            border: '1px solid var(--border-strong)',
            borderRadius: 10,
            fontSize: 15,
            fontWeight: 600,
            background: 'var(--surface)'
          }}
        >
          {topics.map((t) => (
            <option key={t.id} value={t.id}>
              {t.name}
            </option>
          ))}
        </select>
      </div>

      {error && <StatusMessage error={error} />}

      {result ? (
        <div
          style={{
            background: 'var(--dark)',
            borderRadius: 'var(--radius-lg)',
            padding: '40px 44px',
            color: '#fff'
          }}
        >
          <div
            style={{
              fontSize: 12.5,
              color: 'rgba(255,255,255,0.55)',
              marginBottom: 8,
              letterSpacing: '0.06em',
              fontWeight: 700
            }}
          >
            SCORE
          </div>

          <div
            className="tabular"
            style={{
              fontSize: 56,
              fontWeight: 800,
              marginBottom: 28,
              color: 'var(--lime)'
            }}
          >
            {result.quizResult.correctAnswers}/
            {result.quizResult.totalQuestions}
          </div>

          {result.replan.planRegenerated ? (
            <div
              style={{
                padding: '18px 22px',
                background: 'rgba(129,182,228,0.15)',
                borderRadius: 12,
                fontSize: 15,
                color: 'var(--blue)',
                border: '1px solid rgba(129,182,228,0.3)'
              }}
            >
              Study plan updated &mdash; priorities shifted based on this result.
            </div>
          ) : (
            <div
              style={{
                fontSize: 14,
                color: 'rgba(255,255,255,0.6)'
              }}
            >
              {result.replan.message}
            </div>
          )}

          <button
            onClick={() => setResult(null)}
            style={{
              marginTop: 26,
              padding: '12px 24px',
              fontSize: 14,
              fontWeight: 700,
              color: '#fff',
              background: 'rgba(255,255,255,0.1)',
              border: '1px solid rgba(255,255,255,0.2)',
              borderRadius: 10,
              cursor: 'pointer'
            }}
          >
            Take another quiz
          </button>
        </div>
      ) : questions.length === 0 ? (
        <div
          style={{
            background: 'var(--surface)',
            border: '1px solid var(--border)',
            borderRadius: 'var(--radius-lg)',
            padding: '32px',
            textAlign: 'center'
          }}
        >
          <p
            style={{
              fontSize: 14,
              color: 'var(--gray)',
              marginBottom: 16
            }}
          >
            No questions yet for this topic.
          </p>

          <button
            disabled={generating}
            onClick={() => {
              setError('');
              setGenerating(true);
              api.generateQuizQuestions(selectedTopicId, 5)
                .then(() => api.getQuestionsByTopic(selectedTopicId))
                .then(setQuestions)
                .catch((err) => setError(err.message))
                .finally(() => setGenerating(false));
            }}
            style={{
              padding: '11px 22px',
              fontSize: 13.5,
              fontWeight: 700,
              color: 'var(--dark)',
              background: 'var(--lime)',
              border: 'none',
              borderRadius: 10,
              cursor: generating ? 'wait' : 'pointer',
              opacity: generating ? 0.6 : 1
            }}
          >
            {generating ? 'Generating…' : 'Generate 5 questions with AI'}
          </button>
        </div>
      ) : (
        <form onSubmit={handleSubmit}>
          {questions.map((q, i) => (
            <div
              key={q.id}
              style={{
                padding: '30px 0',
                borderBottom: '1px solid var(--border)'
              }}
            >
              <div
                style={{
                  fontSize: 21,
                  fontWeight: 700,
                  marginBottom: 18,
                  letterSpacing: '-0.01em'
                }}
              >
                {i + 1}. <Markdown text={q.questionText} inline />
              </div>

              <div
                style={{
                  display: 'flex',
                  flexDirection: 'column',
                  gap: 10
                }}
              >
                {[
                  ['A', q.optionA],
                  ['B', q.optionB],
                  ['C', q.optionC],
                  ['D', q.optionD]
                ].map(([key, text]) => {
                  const selected = answers[q.id] === key;

                  return (
                    <label
                      key={key}
                      style={{
                        display: 'flex',
                        alignItems: 'center',
                        gap: 14,
                        fontSize: 15.5,
                        cursor: 'pointer',
                        padding: '14px 18px',
                        borderRadius: 12,
                        border: selected
                          ? '1.5px solid var(--dark)'
                          : '1px solid var(--border)',
                        background: selected
                          ? 'var(--bg)'
                          : 'transparent',
                        fontWeight: selected ? 600 : 400
                      }}
                    >
                      <input
                        type="radio"
                        name={`q-${q.id}`}
                        checked={selected}
                        onChange={() => handleSelect(q.id, key)}
                        style={{ accentColor: 'var(--dark)' }}
                      />
                      <Markdown text={text} inline />
                    </label>
                  );
                })}
              </div>
            </div>
          ))}

          <div style={{ padding: '24px 0' }}>
            <button
              type="submit"
              disabled={submitting}
              style={{
                padding: '14px 30px',
                fontSize: 14.5,
                fontWeight: 700,
                color: 'var(--dark)',
                background: 'var(--lime)',
                border: 'none',
                borderRadius: 10,
                cursor: 'pointer'
              }}
            >
              {submitting ? 'Submitting…' : 'Submit quiz'}
            </button>
          </div>
        </form>
      )}
    </div>
  );
}

export default QuizPage;