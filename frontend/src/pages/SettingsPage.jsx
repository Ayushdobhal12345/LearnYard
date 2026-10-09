import { useState } from 'react';
import { getCurrentStudent, loginSuccess } from '../api/client';
import PageHeader from '../components/PageHeader';

const BASE_URL = 'http://localhost:8080/api';

function SettingsPage() {
  const student = getCurrentStudent();
  const [name, setName] = useState(student?.name || '');
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  const handleSave = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    setMessage('');
    try {
      const token = localStorage.getItem('ila_token');
      const res = await fetch(`${BASE_URL}/auth/profile`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
        body: JSON.stringify({ name }),
      });
      if (!res.ok) throw new Error('Could not update profile.');
      const data = await res.json();
      loginSuccess(data.token, { studentId: data.studentId, name: data.name, email: data.email });
      setMessage('Saved.');
    } catch (err) {
      setError(err.message);
    } finally {
      setSaving(false);
    }
  };

  return (
    <div>
      <PageHeader eyebrow="Account" title="Settings" subtitle="Manage your profile." />

      <form onSubmit={handleSave} style={{ maxWidth: 420, background: 'var(--surface)', border: '1px solid var(--border)', borderRadius: 'var(--radius-lg)', padding: '28px 30px' }}>
        <label style={{ display: 'block', fontSize: 12, color: 'var(--gray)', marginBottom: 6, fontWeight: 600 }}>Full name</label>
        <input
          value={name} onChange={(e) => setName(e.target.value)}
          style={{ width: '100%', padding: '12px 14px', borderRadius: 10, border: '1px solid var(--border-strong)', fontSize: 14.5, fontFamily: 'var(--font)', marginBottom: 16 }}
        />

        <label style={{ display: 'block', fontSize: 12, color: 'var(--gray)', marginBottom: 6, fontWeight: 600 }}>Email</label>
        <input
          value={student?.email || ''} disabled
          style={{ width: '100%', padding: '12px 14px', borderRadius: 10, border: '1px solid var(--border)', fontSize: 14.5, fontFamily: 'var(--font)', marginBottom: 20, color: 'var(--gray)', background: 'var(--bg)' }}
        />

        {message && <p style={{ fontSize: 13, color: 'var(--lime-deep)', marginBottom: 12 }}>{message}</p>}
        {error && <p style={{ fontSize: 13, color: '#c0392b', marginBottom: 12 }}>{error}</p>}

        <button
          type="submit" disabled={saving}
          style={{ padding: '12px 24px', fontSize: 14, fontWeight: 700, color: 'var(--dark)', background: 'var(--lime)', border: 'none', borderRadius: 10, cursor: 'pointer' }}
        >
          {saving ? 'Saving…' : 'Save changes'}
        </button>
      </form>
    </div>
  );
}

export default SettingsPage;