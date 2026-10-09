import { useState, useEffect } from 'react';
import { api, getCurrentSubjectId, selectSubject, clearSubject } from '../api/client';
import PageHeader from '../components/PageHeader';
import { useToast } from '../context/ToastContext';

function SubjectsPage() {
  const showToast = useToast();

  const [subjects, setSubjects] = useState([]);
  const [units, setUnits] = useState([]);
  const [topicsByUnit, setTopicsByUnit] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [importMode, setImportMode] = useState('subject');
  const [importUnitId, setImportUnitId] = useState('');

  const [newSubjectName, setNewSubjectName] = useState('');
  const [newUnitName, setNewUnitName] = useState('');
  const [newTopicName, setNewTopicName] = useState('');
  const [newTopicUnitId, setNewTopicUnitId] = useState('');

  const [syllabusFile, setSyllabusFile] = useState(null);
  const [importing, setImporting] = useState(false);
  const [inferring, setInferring] = useState(false);

  const allTopicsFlat = Object.values(topicsByUnit).flat();

  const activeSubjectId = getCurrentSubjectId();

  const [dependencies, setDependencies] = useState([]);
  const [depTopicId, setDepTopicId] = useState('');
  const [depPrereqId, setDepPrereqId] = useState('');

  const handleDeleteSubject = (id, name) => {
    if (!window.confirm(`Delete "${name}"? This removes it permanently.`)) return;

    api.deleteSubject(id)
      .then(() => {
        showToast('Subject deleted.');

        if (id === activeSubjectId) {
          // The deleted subject must not stay selected. Asking the server for its units would
          // fail with "not found", so only refresh the list of remaining subjects.
          clearSubject();
          setUnits([]);
          setTopicsByUnit({});
          setDependencies([]);
          api.getSubjects()
            .then(setSubjects)
            .catch((err) => setError(err.message));
        } else {
          loadAll();
        }
      })
      .catch((err) => setError(err.message));
  };

  const handleDeleteUnit = (id) => {
    if (!window.confirm('Delete this unit?')) return;

    api.deleteUnit(id)
      .then(() => {
        showToast('Unit deleted.');
        loadAll();
      })
      .catch((err) => setError(err.message));
  };

  const handleDeleteTopic = (id) => {
    if (!window.confirm('Delete this topic?')) return;

    api.deleteTopic(id)
      .then(() => {
        showToast('Topic deleted.');
        loadAll();
      })
      .catch((err) => setError(err.message));
  };

  const loadAll = () => {
    api.getSubjects()
      .then(async (subs) => {
        setSubjects(subs);

        if (activeSubjectId) {
          const u = await api.getUnitsBySubject(activeSubjectId);
          setUnits(u);

          const topicsMap = {};

          await Promise.all(
            u.map(async (unit) => {
              topicsMap[unit.id] = await api.getTopicsByUnit(unit.id);
            })
          );

          setTopicsByUnit(topicsMap);

          const deps = await api.getDependenciesForSubject(activeSubjectId);
          setDependencies(deps);
        }
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadAll();
  }, []);

  const handleCreateSubject = (e) => {
    e.preventDefault();

    if (!newSubjectName.trim()) return;

    api.createSubject(newSubjectName, '')
      .then((s) => {
        setNewSubjectName('');
        selectSubject({ id: s.id, name: s.name });
      })
      .catch((err) => setError(err.message));
  };

  const handleImportSyllabus = (e) => {
    e.preventDefault();

    if (!syllabusFile || !activeSubjectId) return;

    setImporting(true);
    setError('');

    const call = importMode === 'subject'
      ? api.importSyllabusForSubject(syllabusFile, activeSubjectId)
      : api.importSyllabusForUnit(syllabusFile, importUnitId);

    call
      .then((res) => {
        showToast(`${res.topicsCreated} topics created.`);
        setSyllabusFile(null);
        loadAll();
      })
      .catch((err) => setError(err.message))
      .finally(() => setImporting(false));
  };

  const handleCreateUnit = (e) => {
    e.preventDefault();

    if (!newUnitName.trim() || !activeSubjectId) return;

    api.createUnit({
      name: newUnitName,
      description: '',
      unitOrder: units.length + 1,
      subjectId: activeSubjectId
    })
      .then(() => {
        setNewUnitName('');
        loadAll();
      })
      .catch((err) => setError(err.message));
  };

  const handleCreateTopic = (e) => {
    e.preventDefault();

    if (!newTopicName.trim() || !newTopicUnitId) return;

    api.createTopic({
      name: newTopicName,
      description: '',
      difficultyLevel: 2,
      unitId: Number(newTopicUnitId)
    })
      .then(() => {
        setNewTopicName('');
        loadAll();
      })
      .catch((err) => setError(err.message));
  };

  const handleCreateDependency = (e) => {
    e.preventDefault();

    if (!depTopicId || !depPrereqId) return;

    if (depTopicId === depPrereqId) {
      setError('A topic cannot depend on itself.');
      return;
    }

    api.createDependency(
      Number(depTopicId),
      Number(depPrereqId)
    )
      .then(() => {
        setDepTopicId('');
        setDepPrereqId('');
        loadAll();
      })
      .catch((err) => setError(err.message));
  };

  const handleDeleteDependency = (id) => {
    api.deleteDependency(id)
      .then(() => loadAll())
      .catch((err) => setError(err.message));
  };

  if (loading) {
    return <p style={{ color: 'var(--gray)' }}>Loading…</p>;
  }

  return (
    <div>
      <PageHeader
        eyebrow="Curriculum"
        title="Your subjects."
        subtitle="Pick, switch, or build out a subject's syllabus."
      />

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

      {/* Subject list + create */}
      <div
        style={{
          background: 'var(--surface)',
          border: '1px solid var(--border)',
          borderRadius: 'var(--radius-lg)',
          padding: '26px 28px',
          marginBottom: 20
        }}
      >
        <div
          style={{
            fontSize: 15,
            fontWeight: 700,
            marginBottom: 14
          }}
        >
          Subjects
        </div>

        {subjects.map((s) => (
          <div
            key={s.id}
            onClick={() => selectSubject({ id: s.id, name: s.name })}
            style={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              padding: '12px 14px',
              borderRadius: 10,
              cursor: 'pointer',
              marginBottom: 6,
              background:
                s.id === activeSubjectId
                  ? 'var(--bg)'
                  : 'transparent',
              border:
                s.id === activeSubjectId
                  ? '1px solid var(--border-strong)'
                  : '1px solid transparent'
            }}
          >
            <span
              style={{
                fontSize: 14,
                fontWeight: 600
              }}
            >
              {s.name}
            </span>

            <button
              onClick={(e) => {
                e.stopPropagation();
                handleDeleteSubject(s.id, s.name);
              }}
              style={{
                background: 'none',
                border: 'none',
                color: '#c0392b',
                fontSize: 12,
                cursor: 'pointer'
              }}
            >
              ✕
            </button>

            {s.id === activeSubjectId && (
              <span
                style={{
                  fontSize: 11,
                  fontWeight: 700,
                  color: 'var(--blue-deep)'
                }}
              >
                ACTIVE
              </span>
            )}
          </div>
        ))}

        {subjects.length === 0 && (
          <p
            style={{
              fontSize: 13.5,
              color: 'var(--gray)'
            }}
          >
            No subjects yet — create one below.
          </p>
        )}

        <form
          onSubmit={handleCreateSubject}
          style={{
            display: 'flex',
            gap: 10,
            marginTop: 14
          }}
        >
          <input
            value={newSubjectName}
            onChange={(e) => setNewSubjectName(e.target.value)}
            placeholder="New subject name (e.g. Operating Systems)"
            style={{
              flex: 1,
              padding: '10px 14px',
              borderRadius: 10,
              border: '1px solid var(--border-strong)',
              fontSize: 14
            }}
          />

          <button
            type="submit"
            style={{
              padding: '10px 20px',
              fontSize: 13,
              fontWeight: 700,
              color: '#fff',
              background: 'var(--blue-deep)',
              border: 'none',
              borderRadius: 10,
              cursor: 'pointer'
            }}
          >
            Add
          </button>
        </form>
      </div>

      {/* Auto-import syllabus */}
      {activeSubjectId && (
        <div
          style={{
            background: 'var(--surface)',
            border: '1px dashed var(--border-strong)',
            borderRadius: 'var(--radius-lg)',
            padding: '22px 26px',
            marginBottom: 20
          }}
        >
          <div
            style={{
              fontSize: 14,
              fontWeight: 700,
              marginBottom: 4
            }}
          >
            Auto-import syllabus
          </div>

          <p
            style={{
              fontSize: 12.5,
              color: 'var(--gray)',
              marginBottom: 14
            }}
          >
            Upload a PDF, text file, or photo — AI extracts the structure automatically.
          </p>

          <div
            style={{
              display: 'flex',
              gap: 8,
              marginBottom: 14
            }}
          >
            <button
              type="button"
              onClick={() => setImportMode('subject')}
              style={{
                padding: '7px 14px',
                fontSize: 12.5,
                fontWeight: 700,
                borderRadius: 8,
                border: '1px solid var(--border-strong)',
                cursor: 'pointer',
                background:
                  importMode === 'subject'
                    ? 'var(--dark)'
                    : 'none',
                color:
                  importMode === 'subject'
                    ? '#fff'
                    : 'var(--dark)'
              }}
            >
              Whole syllabus (creates units + topics)
            </button>

            <button
              type="button"
              onClick={() => setImportMode('unit')}
              style={{
                padding: '7px 14px',
                fontSize: 12.5,
                fontWeight: 700,
                borderRadius: 8,
                border: '1px solid var(--border-strong)',
                cursor: 'pointer',
                background:
                  importMode === 'unit'
                    ? 'var(--dark)'
                    : 'none',
                color:
                  importMode === 'unit'
                    ? '#fff'
                    : 'var(--dark)'
              }}
            >
              One unit (topics only)
            </button>
          </div>

          <form
            onSubmit={handleImportSyllabus}
            style={{
              display: 'flex',
              gap: 12,
              alignItems: 'center',
              flexWrap: 'wrap'
            }}
          >
            {importMode === 'unit' && (
              <select
                value={importUnitId}
                onChange={(e) => setImportUnitId(e.target.value)}
                style={{
                  padding: '9px 12px',
                  borderRadius: 8,
                  border: '1px solid var(--border-strong)',
                  fontSize: 13
                }}
              >
                <option value="">Choose unit…</option>

                {units.map((u) => (
                  <option key={u.id} value={u.id}>
                    {u.name}
                  </option>
                ))}
              </select>
            )}

            <input
              type="file"
              accept=".pdf,.txt,.png,.jpg,.jpeg"
              onChange={(e) => setSyllabusFile(e.target.files[0])}
              style={{
                fontSize: 13
              }}
            />

            <button
              type="submit"
              disabled={
                importing ||
                !syllabusFile ||
                (importMode === 'unit' && !importUnitId)
              }
              style={{
                padding: '9px 18px',
                fontSize: 12.5,
                fontWeight: 700,
                color: 'var(--dark)',
                background: 'var(--lime)',
                border: 'none',
                borderRadius: 8,
                cursor: 'pointer',
                opacity:
                  importing || !syllabusFile
                    ? 0.6
                    : 1
              }}
            >
              {importing ? 'Reading…' : 'Import'}
            </button>
          </form>
        </div>
      )}

      {/* Units + Topics */}
      {activeSubjectId && (
        <div
          style={{
            display: 'grid',
            gridTemplateColumns: '1fr 1fr',
            gap: 20
          }}
        >
          {/* Units */}
          <div
            style={{
              background: 'var(--surface)',
              border: '1px solid var(--border)',
              borderRadius: 'var(--radius-lg)',
              padding: '24px 26px'
            }}
          >
            <div
              style={{
                fontSize: 14,
                fontWeight: 700,
                marginBottom: 12
              }}
            >
              Units
            </div>

            {units.map((u) => (
              <div
                key={u.id}
                style={{
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  fontSize: 13.5,
                  padding: '8px 0',
                  borderBottom: '1px solid var(--border)'
                }}
              >
                <span>{u.name}</span>

                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    handleDeleteUnit(u.id);
                  }}
                  style={{
                    background: 'none',
                    border: 'none',
                    color: '#c0392b',
                    fontSize: 12,
                    cursor: 'pointer'
                  }}
                >
                  ✕
                </button>
              </div>
            ))}

            <form
              onSubmit={handleCreateUnit}
              style={{
                display: 'flex',
                gap: 8,
                marginTop: 12
              }}
            >
              <input
                value={newUnitName}
                onChange={(e) => setNewUnitName(e.target.value)}
                placeholder="New unit name"
                style={{
                  flex: 1,
                  padding: '9px 12px',
                  borderRadius: 8,
                  border: '1px solid var(--border-strong)',
                  fontSize: 13
                }}
              />

              <button
                type="submit"
                style={{
                  padding: '9px 16px',
                  fontSize: 12.5,
                  fontWeight: 700,
                  background: 'var(--bg)',
                  border: '1px solid var(--border-strong)',
                  borderRadius: 8,
                  cursor: 'pointer'
                }}
              >
                Add
              </button>
            </form>
          </div>

          {/* Topics */}
          <div
            style={{
              background: 'var(--surface)',
              border: '1px solid var(--border)',
              borderRadius: 'var(--radius-lg)',
              padding: '24px 26px'
            }}
          >
            <div
              style={{
                fontSize: 14,
                fontWeight: 700,
                marginBottom: 12
              }}
            >
              Topics
            </div>

            {units.map((u) => (
              <div
                key={u.id}
                style={{
                  marginBottom: 10
                }}
              >
                <div
                  style={{
                    fontSize: 11.5,
                    color: 'var(--gray)',
                    marginBottom: 4
                  }}
                >
                  {u.name}
                </div>

                {(topicsByUnit[u.id] || []).map((t) => (
                  <div
                    key={t.id}
                    style={{
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center',
                      fontSize: 13.5,
                      padding: '4px 0'
                    }}
                  >
                    <span>{t.name}</span>

                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        handleDeleteTopic(t.id);
                      }}
                      style={{
                        background: 'none',
                        border: 'none',
                        color: '#c0392b',
                        fontSize: 12,
                        cursor: 'pointer'
                      }}
                    >
                      ✕
                    </button>
                  </div>
                ))}
              </div>
            ))}

            <form
              onSubmit={handleCreateTopic}
              style={{
                display: 'flex',
                flexDirection: 'column',
                gap: 8,
                marginTop: 12
              }}
            >
              <select
                value={newTopicUnitId}
                onChange={(e) => setNewTopicUnitId(e.target.value)}
                style={{
                  padding: '9px 12px',
                  borderRadius: 8,
                  border: '1px solid var(--border-strong)',
                  fontSize: 13
                }}
              >
                <option value="">Choose unit…</option>

                {units.map((u) => (
                  <option key={u.id} value={u.id}>
                    {u.name}
                  </option>
                ))}
              </select>

              <div
                style={{
                  display: 'flex',
                  gap: 8
                }}
              >
                <input
                  value={newTopicName}
                  onChange={(e) => setNewTopicName(e.target.value)}
                  placeholder="New topic name"
                  style={{
                    flex: 1,
                    padding: '9px 12px',
                    borderRadius: 8,
                    border: '1px solid var(--border-strong)',
                    fontSize: 13
                  }}
                />

                <button
                  type="submit"
                  style={{
                    padding: '9px 16px',
                    fontSize: 12.5,
                    fontWeight: 700,
                    background: 'var(--bg)',
                    border: '1px solid var(--border-strong)',
                    borderRadius: 8,
                    cursor: 'pointer'
                  }}
                >
                  Add
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Prerequisites */}
      {activeSubjectId && allTopicsFlat.length >= 2 && (
        <div
          style={{
            background: 'var(--surface)',
            border: '1px solid var(--border)',
            borderRadius: 'var(--radius-lg)',
            padding: '24px 26px',
            marginTop: 20
          }}
        >
          <div
            style={{
              fontSize: 14,
              fontWeight: 700,
              marginBottom: 4
            }}
          >
            Prerequisites
          </div>

          <p
            style={{
              fontSize: 12.5,
              color: 'var(--gray)',
              marginBottom: 14
            }}
          >
            Mark which topics must be learned before others — this powers the Priority Engine and the Dependency Map.
          </p>

          {dependencies.length === 0 ? (
            <p
              style={{
                fontSize: 13,
                color: 'var(--gray)',
                marginBottom: 14
              }}
            >
              No prerequisites defined yet.
            </p>
          ) : (
            <div style={{ marginBottom: 14 }}>
              {dependencies.map((d) => (
                <div
                  key={d.id}
                  style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    padding: '8px 0',
                    borderBottom: '1px solid var(--border)',
                    fontSize: 13.5
                  }}
                >
                  <span>
                    <strong>{d.topicName}</strong> requires{' '}
                    <strong>{d.prerequisiteTopicName}</strong>
                  </span>

                  <button
                    onClick={() => handleDeleteDependency(d.id)}
                    style={{
                      background: 'none',
                      border: 'none',
                      color: '#c0392b',
                      fontSize: 12,
                      cursor: 'pointer'
                    }}
                  >
                    ✕ remove
                  </button>
                </div>
              ))}
            </div>
          )}

          <button
            type="button"
            disabled={inferring}
            onClick={() => {
              setError('');
              setInferring(true);

              api.inferDependencies(activeSubjectId)
                .then((res) => {
                  showToast(
                    `AI identified ${res.dependenciesCreated} prerequisite relationships.`
                  );
                  loadAll();
                })
                .catch((err) => setError(err.message))
                .finally(() => setInferring(false));
            }}
            style={{
              padding: '10px 20px',
              fontSize: 13,
              fontWeight: 700,
              background: 'var(--lime)',
              color: 'var(--dark)',
              border: 'none',
              borderRadius: 10,
              cursor: inferring ? 'wait' : 'pointer',
              opacity: inferring ? 0.6 : 1
            }}
          >
            {inferring ? 'Analyzing topics…' : 'Auto-detect prerequisites with AI'}
          </button>

          <form
            onSubmit={handleCreateDependency}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: 10,
              flexWrap: 'wrap',
              marginTop: 14
            }}
          >
            <select
              value={depTopicId}
              onChange={(e) => setDepTopicId(e.target.value)}
              style={{
                padding: '9px 12px',
                borderRadius: 8,
                border: '1px solid var(--border-strong)',
                fontSize: 13
              }}
            >
              <option value="">This topic…</option>

              {allTopicsFlat.map((t) => (
                <option key={t.id} value={t.id}>
                  {t.name}
                </option>
              ))}
            </select>

            <span
              style={{
                fontSize: 13,
                color: 'var(--gray)'
              }}
            >
              requires
            </span>

            <select
              value={depPrereqId}
              onChange={(e) => setDepPrereqId(e.target.value)}
              style={{
                padding: '9px 12px',
                borderRadius: 8,
                border: '1px solid var(--border-strong)',
                fontSize: 13
              }}
            >
              <option value="">this prerequisite…</option>

              {allTopicsFlat.map((t) => (
                <option key={t.id} value={t.id}>
                  {t.name}
                </option>
              ))}
            </select>

            <button
              type="submit"
              style={{
                padding: '9px 18px',
                fontSize: 12.5,
                fontWeight: 700,
                background: 'var(--dark)',
                color: '#fff',
                border: 'none',
                borderRadius: 8,
                cursor: 'pointer'
              }}
            >
              Add
            </button>
          </form>
        </div>
      )}
    </div>
  );
}

export default SubjectsPage;