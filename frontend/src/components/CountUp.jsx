import { useEffect, useRef } from "react";
import { animate } from "framer-motion";

// Animates a numeric value's on-screen text from its previous value to
// the new one whenever `value` changes — used for the KPI tiles so
// polling updates feel alive instead of just snapping.
export default function CountUp({ value, decimals = 0, suffix = "" }) {
  const ref = useRef(null);
  const prevValue = useRef(0);

  useEffect(() => {
    const node = ref.current;
    if (!node) return;
    const from = prevValue.current;
    const to = typeof value === "number" ? value : 0;
    const controls = animate(from, to, {
      duration: 0.6,
      ease: "easeOut",
      onUpdate(latest) {
        node.textContent = latest.toFixed(decimals) + suffix;
      },
    });
    prevValue.current = to;
    return () => controls.stop();
  }, [value, decimals, suffix]);

  return <span ref={ref} className="tabular-nums">0{suffix}</span>;
}
