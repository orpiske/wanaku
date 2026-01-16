import React, { useState, useRef, useCallback } from "react";
import {
  Button,
  Select,
  SelectItem,
  TextArea,
  ToastNotification,
  InlineLoading,
  NumberInput,
  Tile,
  Grid,
  Column,
} from "@carbon/react";
import { Play, Stop } from "@carbon/icons-react";
import { getPostApiV2CodeExecutionEngineEngineTypeLanguageUrl } from "../../api/wanaku-router-api";
import { CodeExecutionRequest } from "../../models";
import "./CodeExecutionPage.scss";

interface ExecutionEvent {
  eventType: string;
  taskId: string;
  content?: string;
  exitCode?: number;
  errorMessage?: string;
  timestamp: string;
}

const ENGINE_TYPES = [
  { value: "jvm", label: "JVM (Java, Kotlin, Scala)" },
  { value: "interpreted", label: "Interpreted (Python, JavaScript)" },
  { value: "native", label: "Native (Go, Rust)" },
];

const LANGUAGES: Record<string, { value: string; label: string }[]> = {
  jvm: [
    { value: "java", label: "Java" },
    { value: "kotlin", label: "Kotlin" },
    { value: "scala", label: "Scala" },
  ],
  interpreted: [
    { value: "python", label: "Python" },
    { value: "javascript", label: "JavaScript" },
    { value: "ruby", label: "Ruby" },
  ],
  native: [
    { value: "go", label: "Go" },
    { value: "rust", label: "Rust" },
  ],
};

const DEFAULT_CODE: Record<string, string> = {
  java: `public class Main {
    public static void main(String[] args) {
        System.out.println("Hello, World!");
    }
}`,
  python: `print("Hello, World!")`,
  javascript: `console.log("Hello, World!");`,
  kotlin: `fun main() {
    println("Hello, World!")
}`,
  scala: `object Main extends App {
  println("Hello, World!")
}`,
  ruby: `puts "Hello, World!"`,
  go: `package main

import "fmt"

func main() {
    fmt.Println("Hello, World!")
}`,
  rust: `fn main() {
    println!("Hello, World!");
}`,
};

const CodeExecutionPage: React.FC = () => {
  const [engineType, setEngineType] = useState<string>("interpreted");
  const [language, setLanguage] = useState<string>("python");
  const [code, setCode] = useState<string>(DEFAULT_CODE["python"] || "");
  const [timeout, setTimeout] = useState<number>(30);
  const [output, setOutput] = useState<ExecutionEvent[]>([]);
  const [isExecuting, setIsExecuting] = useState<boolean>(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [taskId, setTaskId] = useState<string | null>(null);

  const eventSourceRef = useRef<EventSource | null>(null);
  const outputRef = useRef<HTMLDivElement>(null);

  const scrollToBottom = useCallback(() => {
    if (outputRef.current) {
      outputRef.current.scrollTop = outputRef.current.scrollHeight;
    }
  }, []);

  const handleEngineTypeChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const newEngineType = e.target.value;
    setEngineType(newEngineType);

    // Reset language to first available for the new engine type
    const availableLanguages = LANGUAGES[newEngineType] || [];
    if (availableLanguages.length > 0) {
      const newLanguage = availableLanguages[0].value;
      setLanguage(newLanguage);
      setCode(DEFAULT_CODE[newLanguage] || "");
    }
  };

  const handleLanguageChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const newLanguage = e.target.value;
    setLanguage(newLanguage);
    setCode(DEFAULT_CODE[newLanguage] || "");
  };

  const stopExecution = useCallback(() => {
    if (eventSourceRef.current) {
      eventSourceRef.current.close();
      eventSourceRef.current = null;
    }
    setIsExecuting(false);
  }, []);

  const executeCode = async () => {
    // Clear previous output
    setOutput([]);
    setErrorMessage(null);
    setIsExecuting(true);

    try {
      // Encode code as base64
      const encodedCode = btoa(code);

      const request: CodeExecutionRequest = {
        code: encodedCode,
        timeout: timeout,
        arguments: [],
        environment: {},
      };

      const baseUrl = import.meta.env.VITE_API_URL || window.location.origin;
      const url = baseUrl + getPostApiV2CodeExecutionEngineEngineTypeLanguageUrl(engineType, language);

      const response = await fetch(url, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(request),
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const result = await response.json();
      const sseUrl = result.sseUrl;
      setTaskId(result.taskId);

      // Connect to SSE stream
      const eventSource = new EventSource(sseUrl);
      eventSourceRef.current = eventSource;

      eventSource.onopen = () => {
        console.log("SSE connection opened");
      };

      eventSource.addEventListener("started", (event) => {
        const data = JSON.parse(event.data) as ExecutionEvent;
        setOutput((prev) => [...prev, { ...data, timestamp: new Date().toISOString() }]);
        scrollToBottom();
      });

      eventSource.addEventListener("output", (event) => {
        const data = JSON.parse(event.data) as ExecutionEvent;
        setOutput((prev) => [...prev, { ...data, timestamp: new Date().toISOString() }]);
        scrollToBottom();
      });

      eventSource.addEventListener("completed", (event) => {
        const data = JSON.parse(event.data) as ExecutionEvent;
        setOutput((prev) => [...prev, { ...data, timestamp: new Date().toISOString() }]);
        scrollToBottom();
        stopExecution();
      });

      eventSource.addEventListener("failed", (event) => {
        const data = JSON.parse(event.data) as ExecutionEvent;
        setOutput((prev) => [...prev, { ...data, timestamp: new Date().toISOString() }]);
        scrollToBottom();
        stopExecution();
      });

      eventSource.addEventListener("error", (event) => {
        console.error("SSE error:", event);
        const messageEvent = event as MessageEvent;
        if (messageEvent.data) {
          const data = JSON.parse(messageEvent.data) as ExecutionEvent;
          setOutput((prev) => [...prev, { ...data, timestamp: new Date().toISOString() }]);
        }
        stopExecution();
      });

      eventSource.onerror = (error) => {
        console.error("SSE connection error:", error);
        stopExecution();
      };

    } catch (error) {
      console.error("Execution error:", error);
      setErrorMessage(error instanceof Error ? error.message : "Failed to execute code");
      setIsExecuting(false);
    }
  };

  const getOutputClass = (eventType: string): string => {
    switch (eventType?.toLowerCase()) {
      case "started":
        return "output-started";
      case "completed":
        return "output-completed";
      case "failed":
      case "error":
        return "output-error";
      default:
        return "output-standard";
    }
  };

  const formatOutput = (event: ExecutionEvent): string => {
    switch (event.eventType?.toLowerCase()) {
      case "started":
        return `[STARTED] Execution started (Task: ${event.taskId})`;
      case "completed":
        return `[COMPLETED] Execution finished with exit code: ${event.exitCode}`;
      case "failed":
        return `[FAILED] ${event.errorMessage || "Execution failed"} (Exit code: ${event.exitCode})`;
      case "output":
        return event.content || "";
      default:
        return event.content || JSON.stringify(event);
    }
  };

  return (
    <div className="code-execution-page">
      {errorMessage && (
        <ToastNotification
          kind="error"
          title="Error"
          subtitle={errorMessage}
          onCloseButtonClick={() => setErrorMessage(null)}
          timeout={10000}
          style={{ position: "fixed", top: "60px", right: "20px", zIndex: 1000 }}
        />
      )}

      <h1 className="title">Code Execution</h1>
      <p className="description">
        Execute code remotely using the Wanaku code execution engine.
      </p>

      <Grid className="execution-grid">
        <Column lg={8} md={8} sm={4}>
          <Tile className="code-input-tile">
            <div className="controls-row">
              <Select
                id="engine-type"
                labelText="Engine Type"
                value={engineType}
                onChange={handleEngineTypeChange}
                disabled={isExecuting}
              >
                {ENGINE_TYPES.map((engine) => (
                  <SelectItem key={engine.value} value={engine.value} text={engine.label} />
                ))}
              </Select>

              <Select
                id="language"
                labelText="Language"
                value={language}
                onChange={handleLanguageChange}
                disabled={isExecuting}
              >
                {(LANGUAGES[engineType] || []).map((lang) => (
                  <SelectItem key={lang.value} value={lang.value} text={lang.label} />
                ))}
              </Select>

              <NumberInput
                id="timeout"
                label="Timeout (seconds)"
                value={timeout}
                min={1}
                max={300}
                onChange={(_, { value }) => setTimeout(value as number)}
                disabled={isExecuting}
              />
            </div>

            <TextArea
              id="code-editor"
              labelText="Code"
              value={code}
              onChange={(e) => setCode(e.target.value)}
              rows={15}
              disabled={isExecuting}
              className="code-textarea"
            />

            <div className="action-buttons">
              {!isExecuting ? (
                <Button
                  kind="primary"
                  renderIcon={Play}
                  onClick={executeCode}
                  disabled={!code.trim()}
                >
                  Execute
                </Button>
              ) : (
                <>
                  <InlineLoading description="Executing..." />
                  <Button kind="danger" renderIcon={Stop} onClick={stopExecution}>
                    Stop
                  </Button>
                </>
              )}
            </div>
          </Tile>
        </Column>

        <Column lg={8} md={8} sm={4}>
          <Tile className="output-tile">
            <h4>Output {taskId && <span className="task-id">(Task: {taskId})</span>}</h4>
            <div className="output-console" ref={outputRef}>
              {output.length === 0 ? (
                <div className="output-placeholder">
                  Output will appear here after execution...
                </div>
              ) : (
                output.map((event, index) => (
                  <div key={index} className={`output-line ${getOutputClass(event.eventType)}`}>
                    {formatOutput(event)}
                  </div>
                ))
              )}
            </div>
          </Tile>
        </Column>
      </Grid>
    </div>
  );
};

export const Component = CodeExecutionPage;
