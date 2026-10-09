import { useState, useEffect } from 'react';

const API_BASE_URL = 'http://localhost:8080/api/subjects';

function SubjectList() {
  const [subjects, setSubjects] = useState([]);
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  const fetchSubjects = () => {
    setLoading(true);
    fetch(API_BASE_URL)
      .then((res) => res.json())
      .then((data) => {
        setSubjects(data);
        setLoading(false);
      })
      .catch((err) => {
        console.error('Error fetching subjects:', err);
        setError('Could not load subjects. Is the backend running?');
        setLoading(false);
      });
  };

  useEffect(() => {
    fetchSubjects();
  }, []);

  const handleSubmit = (e) => {
    e.preventDefault();
    setError('');

    fetch(API_BASE_URL, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name, description }),
    })
      .then((res) => {
        if (!res.ok) {
          return res.json().then((errBody) => {
            throw new Error(errBody.message || 'Failed to create subject');
          });
        }
        return res.json();
      })
      .then(() => {
        setName('');
        setDescription('');
        fetchSubjects();
      })
      .catch((err) => {
        setError(err.message);
      });
  };

  return (
    <div style={{ maxWidth: 600, margin: '40px auto', fontFamily: 'sans-serif' }}>
      <h1>Subjects</h1>

      <form onSubmit={handleSubmit} style={{ marginBottom: 24 }}>
        <div>
          <input
            type="text"
            placeholder="Subject name"
            value={name}
            onChange={(e) => setName(e.target.value)}
            required
          />
        </div>
        <div style={{ marginTop: 8 }}>
          <input
            type="text"
            placeholder="Description"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            style={{ width: '100%' }}
          />
        </div>
        <button type="submit" style={{ marginTop: 8 }}>
          Add Subject
        </button>
      </form>

      {error && <p style={{ color: 'red' }}>{error}</p>}

      {loading ? (
        <p>Loading subjects...</p>
      ) : subjects.length === 0 ? (
        <p>No subjects yet. Add one above.</p>
      ) : (
        <ul>
          {subjects.map((subject) => (
            <li key={subject.id}>
              <strong>{subject.name}</strong>
              {subject.description ? ` — ${subject.description}` : ''}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

export default SubjectList;