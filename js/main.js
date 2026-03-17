const root = document.getElementById("app");

if (root) {
  root.innerHTML = `
    <main style="font-family: ui-serif, Georgia, serif; padding: 3rem; max-width: 42rem; margin: 0 auto;">
      <h1 style="font-size: 2.5rem; margin-bottom: 1rem;">Electric Ensemble</h1>
      <p style="font-size: 1.125rem; line-height: 1.6;">
        Electric starter scaffolding is in place. The next code step is to replace this shell with the live
        Electric entrypoint and move the existing MIDI rendering flow behind it.
      </p>
    </main>
  `;
}
