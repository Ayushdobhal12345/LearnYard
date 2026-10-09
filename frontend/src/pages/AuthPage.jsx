import { useState } from 'react';
import { authApi, loginSuccess } from '../api/client';

function AuthPage({ onSuccess, initialMode = 'login', onBack }) {
  const [mode, setMode] = useState(initialMode);
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    const call = mode === 'login'
      ? authApi.login(email, password)
      : authApi.register(name, email, password);

    call
      .then((res) => {
        loginSuccess(res.token, { studentId: res.studentId, name: res.name, email: res.email });
        onSuccess();
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  };

  return (
    <div
      style={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        background: 'var(--dark)',
      }}
    >
      <div style={{ width: 380, maxWidth: 'calc(100vw - 32px)', background: 'var(--bg)', borderRadius: 20, padding: '40px 36px' }}>
        {onBack && (
          <button
            type="button"
            onClick={onBack}
            style={{ background: 'none', border: 'none', padding: 0, marginBottom: 18, fontSize: 13, fontWeight: 600, color: 'var(--gray)', cursor: 'pointer' }}
          >
            ← Back to home
          </button>
        )}
        <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 32 }}>
          <div
            style={{
              width: 34, height: 34, borderRadius: 9, background: 'var(--lime)',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              fontSize: 14, fontWeight: 800, color: 'var(--dark)',
            }}
          >
            L
          </div>
          <div style={{ fontSize: 18, fontWeight: 800 }}>LearnYard</div>
        </div>

        <h1 style={{ fontSize: 28, fontWeight: 800, margin: '0 0 6px', letterSpacing: '-0.01em' }}>
          {mode === 'login' ? 'Welcome back' : 'Create your account'}
        </h1>
        <p style={{ fontSize: 13.5, color: 'var(--gray)', margin: '0 0 28px' }}>
          {mode === 'login' ? 'Log in to continue studying.' : 'Start tracking your mastery today.'}
        </p>

        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
          {mode === 'register' && (
            <input
              type="text" placeholder="Full name" value={name}
              onChange={(e) => setName(e.target.value)} required
              style={{ padding: '13px 16px', borderRadius: 10, border: '1px solid var(--border-strong)', fontSize: 14.5, fontFamily: 'var(--font)' }}
            />
          )}
          <input
            type="email" placeholder="Email" value={email}
            onChange={(e) => setEmail(e.target.value)} required
            style={{ padding: '13px 16px', borderRadius: 10, border: '1px solid var(--border-strong)', fontSize: 14.5, fontFamily: 'var(--font)' }}
          />
          <input
            type="password" placeholder="Password" value={password}
            onChange={(e) => setPassword(e.target.value)} required minLength={6}
            style={{ padding: '13px 16px', borderRadius: 10, border: '1px solid var(--border-strong)', fontSize: 14.5, fontFamily: 'var(--font)' }}
          />

          {error && <p style={{ fontSize: 13, color: '#c0392b', margin: 0 }}>{error}</p>}

          <button
            type="submit" disabled={loading}
            style={{
              padding: '14px', fontSize: 14.5, fontWeight: 700, color: 'var(--dark)',
              background: 'var(--lime)', border: 'none', borderRadius: 10, cursor: 'pointer', marginTop: 6,
            }}
          >
            {loading ? 'Please wait…' : mode === 'login' ? 'Log in' : 'Create account'}
          </button>
        </form>

        <p style={{ fontSize: 13, color: 'var(--gray)', textAlign: 'center', marginTop: 22 }}>
          {mode === 'login' ? "Don't have an account? " : 'Already have an account? '}
          <span
            onClick={() => { setMode(mode === 'login' ? 'register' : 'login'); setError(''); }}
            style={{ color: 'var(--dark)', fontWeight: 700, cursor: 'pointer' }}
          >
            {mode === 'login' ? 'Sign up' : 'Log in'}
          </span>
        </p>
      </div>
    </div>
  );
}

export default AuthPage;