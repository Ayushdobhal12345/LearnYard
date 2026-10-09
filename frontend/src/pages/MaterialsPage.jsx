import { useState, useEffect } from 'react';
import { api, getCurrentSubjectId } from '../api/client';
import PageHeader from '../components/PageHeader';

function MaterialsPage() {
  const [topics, setTopics] = useState([]);
  const [selectedTopicId, setSelectedTopicId] = useState('');

  const [scope, setScope] = useState('topic'); // 'topic' or 'unit'
  const [units, setUnits] = useState([]);
  const [selectedUnitId, setSelectedUnitId] = useState('');

  const [materials, setMaterials] = useState([]);
  const [file, setFile] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    const subjectId = getCurrentSubjectId();
    if (!subjectId) return;

    api.getTopicsForSubject(subjectId).then((t) => {
      setTopics(t);
      if (t.length > 0) setSelectedTopicId(String(t[0].id));
    });

    api.getUnitsBySubject(subjectId).then((u) => {
      setUnits(u);
      if (u.length > 0) setSelectedUnitId(String(u[0].id));
    });
  }, []);

  useEffect(() => {
    if (scope === 'topic' && selectedTopicId) {
      api.getMaterialsByTopic(selectedTopicId)
        .then(setMaterials)
        .catch(() => setMaterials([]));
    } else if (scope === 'unit' && selectedUnitId) {
      api.getMaterialsByUnit(selectedUnitId)
        .then(setMaterials)
        .catch(() => setMaterials([]));
    }
  }, [scope, selectedTopicId, selectedUnitId]);

  const handleUpload = (e) => {
    e.preventDefault();
    if (!file) return;

    setUploading(true);
    setError('');

    const call = scope === 'topic'
      ? api.uploadMaterial(file, selectedTopicId)
      : api.uploadMaterialForUnit(file, selectedUnitId);

    call
      .then(() =>
        scope === 'topic'
          ? api.getMaterialsByTopic(selectedTopicId)
          : api.getMaterialsByUnit(selectedUnitId)
      )
      .then(setMaterials)
      .then(() => setFile(null))
      .catch((err) => setError(err.message))
      .finally(() => setUploading(false));
  };

  return (
    <div>
      <PageHeader
        eyebrow="Study material"
        title="Feed the assistant your notes."
        subtitle="Uploaded files are chunked, embedded, and searchable by the AI."
      />

      <div style={{ display: 'flex', gap: 8, marginBottom: 14 }}>
        <button
          onClick={() => setScope('topic')}
          style={{
            padding: '7px 14px',
            fontSize: 12.5,
            fontWeight: 700,
            borderRadius: 8,
            border: '1px solid var(--border-strong)',
            cursor: 'pointer',
            background: scope === 'topic' ? 'var(--dark)' : 'none',
            color: scope === 'topic' ? '#fff' : 'var(--dark)'
          }}
        >
          By topic
        </button>

        <button
          onClick={() => setScope('unit')}
          style={{
            padding: '7px 14px',
            fontSize: 12.5,
            fontWeight: 700,
            borderRadius: 8,
            border: '1px solid var(--border-strong)',
            cursor: 'pointer',
            background: scope === 'unit' ? 'var(--dark)' : 'none',
            color: scope === 'unit' ? '#fff' : 'var(--dark)'
          }}
        >
          By unit
        </button>
      </div>

      <div style={{ marginBottom: 20 }}>
        {scope === 'topic' ? (
          <select
            value={selectedTopicId}
            onChange={(e) => setSelectedTopicId(e.target.value)}
            style={{
              padding: '10px 14px',
              borderRadius: 10,
              border: '1px solid var(--border-strong)',
              fontSize: 14
            }}
          >
            {topics.length === 0 && (
              <option>No topics in this subject yet</option>
            )}

            {topics.map((t) => (
              <option key={t.id} value={t.id}>
                {t.name}
              </option>
            ))}
          </select>
        ) : (
          <select
            value={selectedUnitId}
            onChange={(e) => setSelectedUnitId(e.target.value)}
            style={{
              padding: '10px 14px',
              borderRadius: 10,
              border: '1px solid var(--border-strong)',
              fontSize: 14
            }}
          >
            {units.length === 0 && (
              <option>No units in this subject yet</option>
            )}

            {units.map((u) => (
              <option key={u.id} value={u.id}>
                {u.name}
              </option>
            ))}
          </select>
        )}
      </div>

      <form
        onSubmit={handleUpload}
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: 14,
          padding: '20px 24px',
          background: 'var(--surface)',
          border: '1px dashed var(--border-strong)',
          borderRadius: 'var(--radius-lg)',
          marginBottom: 24
        }}
      >
        <input
          type="file"
          accept=".pdf,.txt,.png,.jpg,.jpeg"
          onChange={(e) => setFile(e.target.files[0])}
          style={{ fontSize: 13.5 }}
        />

        <button
          type="submit"
          disabled={
            uploading ||
            !file ||
            (scope === 'topic' ? !selectedTopicId : !selectedUnitId)
          }
          style={{
            padding: '10px 22px',
            fontSize: 13.5,
            fontWeight: 700,
            color: 'var(--dark)',
            background: 'var(--lime)',
            border: 'none',
            borderRadius: 10,
            cursor: 'pointer',
            opacity:
              uploading ||
              !file ||
              (scope === 'topic' ? !selectedTopicId : !selectedUnitId)
                ? 0.6
                : 1
          }}
        >
          {uploading ? 'Processing…' : 'Upload'}
        </button>
      </form>

      {error && (
        <p
          style={{
            fontSize: 13,
            color: '#c0392b',
            marginBottom: 16
          }}
        >
          {error}
        </p>
      )}

      <div
        style={{
          background: 'var(--surface)',
          border: '1px solid var(--border)',
          borderRadius: 'var(--radius-lg)',
          padding: '20px 24px'
        }}
      >
        <div
          style={{
            fontSize: 14,
            fontWeight: 700,
            marginBottom: 12
          }}
        >
          Uploaded for this {scope}
        </div>

        {materials.length === 0 ? (
          <p
            style={{
              fontSize: 13.5,
              color: 'var(--gray)'
            }}
          >
            Nothing uploaded yet.
          </p>
        ) : (
          materials.map((m) => (
            <div
              key={m.id}
              style={{
                display: 'flex',
                justifyContent: 'space-between',
                padding: '10px 0',
                borderBottom: '1px solid var(--border)',
                fontSize: 13.5
              }}
            >
              <span>{m.originalFileName}</span>

              <span
                style={{
                  fontSize: 11.5,
                  fontWeight: 700,
                  color: m.isProcessed
                    ? 'var(--lime-deep)'
                    : 'var(--gray)'
                }}
              >
                {m.isProcessed ? 'READY' : 'PROCESSING'}
              </span>
            </div>
          ))
        )}
      </div>
    </div>
  );
}

export default MaterialsPage;