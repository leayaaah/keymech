import { useCallback, useEffect, useState } from 'react';

/**
 * Gọi một hàm bất đồng bộ khi mount và khi `deps` đổi; trả về trạng thái đang tải, lỗi và hàm tải lại.
 * Bỏ qua kết quả của lời gọi cũ khi component unmount hoặc deps đã đổi.
 */
export default function useAsyncData(fn, deps) {
  const [state, setState] = useState({ data: null, loading: true, error: null });
  const [tick, setTick] = useState(0);

  useEffect(() => {
    let cancelled = false;
    setState((s) => ({ ...s, loading: true, error: null }));
    fn()
      .then((data) => !cancelled && setState({ data, loading: false, error: null }))
      .catch((error) => !cancelled && setState({ data: null, loading: false, error }));
    return () => {
      cancelled = true;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [...deps, tick]);

  const reload = useCallback(() => setTick((t) => t + 1), []);
  return { ...state, reload };
}
