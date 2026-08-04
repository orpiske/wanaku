import {createHashRouter} from "react-router-dom";
import App from "./App";
import {ErrorPage} from "./Pages/Error";
import {Links} from "./router/links.models";

export const router = createHashRouter([
  {
    path: Links.Home,
    element: <App />,
    errorElement: <ErrorPage />,
    children: [
      {
        index: true,
        lazy: async () => import("./Pages/Dashboard"),
      },
      {
        path: Links.LLMChat,
        lazy: async () => import("./Pages/LLMChat"),
      },
      {
        path: Links.CodeExecution,
        lazy: async () => import("./Pages/CodeExecution"),
      },
      {
        path: Links.ToolCalls,
        lazy: async () => import("./Pages/ToolCalls"),
      },
      {
        path: Links.Capabilities,
        lazy: async () => import("./Pages/Targets"),
      },
      {
        path: Links.DataStores,
        lazy: async () => import("./Pages/DataStores"),
      },
      {
        path: Links.ServiceCatalog,
        lazy: async () => import("./Pages/ServiceCatalog"),
      },
    ],
  },
]);
