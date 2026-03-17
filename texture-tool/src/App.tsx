import { useState } from "react";
import SingleMode from "./components/SingleMode";
import BatchMode from "./components/BatchMode";
import "./App.css";

type Tab = "single" | "batch";

export default function App() {
  const [tab, setTab] = useState<Tab>("single");

  return (
    <div className="app">
      <header className="header">
        <div className="header-inner">
          <div className="logo">
            <span className="logo-icon">⛏</span>
            <span className="logo-text">Minecraft Texture Tool</span>
          </div>
          <nav className="tabs">
            <button
              className={`tab-btn${tab === "single" ? " active" : ""}`}
              onClick={() => setTab("single")}
            >
              Single Texture
            </button>
            <button
              className={`tab-btn${tab === "batch" ? " active" : ""}`}
              onClick={() => setTab("batch")}
            >
              Batch Mode
            </button>
          </nav>
        </div>
      </header>
      <main className="main">
        {tab === "single" ? <SingleMode /> : <BatchMode />}
      </main>
    </div>
  );
}
