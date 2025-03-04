function App() {
  return (
    <button onClick={() => fetch('/handler?id=1')}>Trigger Handler</button>
  );
}
