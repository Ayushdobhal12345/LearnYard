import { useState, useEffect } from 'react';
import { api, getCurrentSubjectId } from '../api/client';

// A student with no subject yet has nothing set up. This lets the pages that need a curriculum
// show their "add your curriculum first" guidance straight away, without waiting for the server.
const NOTHING_SET_UP = { unitCount: 0, topicCount: 0, materialCount: 0, noSubject: true };

/**
 * What the student has set up for the current subject: { unitCount, topicCount, materialCount }.
 * Reloads whenever refreshKey changes (the app passes the open tab), so after adding topics or
 * uploading notes the next page already sees them.
 *
 * Returns null while loading, and { unavailable: true } if the status could not be read, in which
 * case the app simply lets the page load instead of blocking the student.
 */
export default function useSetupStatus(refreshKey, enabled) {
  const [result, setResult] = useState(null);
  const noSubject = enabled && !getCurrentSubjectId();

  useEffect(() => {
    const subjectId = getCurrentSubjectId();
    if (!enabled || !subjectId) return undefined;

    let cancelled = false;
    api.getSetupStatus(subjectId)
      .then((data) => {
        if (!cancelled) setResult({ key: refreshKey, data });
      })
      .catch(() => {
        if (!cancelled) setResult({ key: refreshKey, data: { unavailable: true } });
      });

    return () => {
      cancelled = true;
    };
  }, [refreshKey, enabled]);

  if (noSubject) return NOTHING_SET_UP;

  // only trust a result that was loaded for the tab that is open now
  return result && result.key === refreshKey ? result.data : null;
}
