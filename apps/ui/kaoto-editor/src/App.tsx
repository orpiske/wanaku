import '@patternfly/react-core/dist/styles/base.css';
import '@patternfly/patternfly/utilities/Accessibility/accessibility.css';
import '@patternfly/patternfly/utilities/Display/display.css';
import '@patternfly/patternfly/utilities/Flex/flex.css';
import '@patternfly/patternfly/utilities/Sizing/sizing.css';
import '@patternfly/patternfly/utilities/Spacing/spacing.css';
import './kaoto-fullscreen.css';

import React, { useCallback, useContext, useEffect, useMemo, useRef, useState } from 'react';
import { VisualizationProvider } from '@patternfly/react-topology';
import { Visualization } from '@kaoto/kaoto';
import {
  EntitiesContext,
  EntitiesProvider,
  ReloadProvider,
  RuntimeProvider,
  SchemasLoaderProvider,
  VisibleFlowsContext,
  VisibleFlowsProvider,
} from '@kaoto/kaoto/lib/providers';
import { CatalogLoaderProvider } from '@kaoto/kaoto/lib/dynamic-catalog/catalog.provider';
import { ControllerService } from '@kaoto/kaoto/lib/components/Visualization/Canvas/controller.service';
import { EventNotifier } from '@kaoto/kaoto/lib/utils';

interface PostMessagePayload {
  type: 'ready' | 'save' | 'load' | 'load-new' | 'request-save';
  yaml?: string;
}

const DEFAULT_YAML = `- route:
    id: new-route
    from:
      uri: direct:wanaku
      steps:
        - log:
            message: "New route"
`;

const CATALOG_URL = './camel-catalog/index.json';

const StableFlowsVisualization: React.FC<{ className?: string }> = ({ className = '' }) => {
  const { visualFlowsApi } = useContext(VisibleFlowsContext)!;
  const entitiesContext = useContext(EntitiesContext);
  const visualEntities = entitiesContext?.visualEntities ?? [];
  const didShowRef = useRef(false);

  useEffect(() => {
    if (!didShowRef.current) {
      didShowRef.current = true;
      visualFlowsApi.showFlows();
    }
  }, [visualFlowsApi]);

  return <Visualization className={`canvas-page ${className}`} entities={visualEntities} />;
};

const Viz: React.FC<{ catalogUrl: string; className?: string }> = ({ catalogUrl, className = '' }) => {
  const controller = useMemo(() => ControllerService.createController(), []);
  return (
    <ReloadProvider>
      <RuntimeProvider catalogUrl={catalogUrl}>
        <SchemasLoaderProvider>
          <CatalogLoaderProvider>
            <VisualizationProvider controller={controller}>
              <VisibleFlowsProvider>
                <StableFlowsVisualization className={className} />
              </VisibleFlowsProvider>
            </VisualizationProvider>
          </CatalogLoaderProvider>
        </SchemasLoaderProvider>
      </RuntimeProvider>
    </ReloadProvider>
  );
};

const App: React.FC = () => {
  const [code, setCode] = useState(DEFAULT_YAML);
  const codeRef = useRef(code);
  const eventNotifier = useMemo(() => EventNotifier.getInstance(), []);

  const handleCodeChange = useCallback((newCode: string) => {
    codeRef.current = newCode;
  }, []);

  useEffect(() => {
    return eventNotifier.subscribe('entities:updated', (updatedCode: string) => {
      handleCodeChange(updatedCode);
    });
  }, [eventNotifier, handleCodeChange]);

  useEffect(() => {
    eventNotifier.next('code:updated', { code });
  }, [code, eventNotifier]);

  useEffect(() => {
    window.parent.postMessage({ type: 'ready' }, '*');
  }, []);

  useEffect(() => {
    const handleMessage = (event: MessageEvent<PostMessagePayload>) => {
      const { type, yaml } = event.data;

      if (type === 'load' && yaml) {
        setCode(yaml);
      } else if (type === 'load-new') {
        setCode(DEFAULT_YAML);
      } else if (type === 'request-save') {
        window.parent.postMessage({ type: 'save', yaml: codeRef.current }, '*');
      }
    };

    window.addEventListener('message', handleMessage);
    return () => window.removeEventListener('message', handleMessage);
  }, []);

  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if ((e.ctrlKey || e.metaKey) && e.key === 's') {
        e.preventDefault();
        window.parent.postMessage({ type: 'save', yaml: codeRef.current }, '*');
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, []);

  return (
    <div style={{ width: '100%', height: '100vh' }}>
      <EntitiesProvider>
        <Viz catalogUrl={CATALOG_URL} />
      </EntitiesProvider>
    </div>
  );
};

export default App;
