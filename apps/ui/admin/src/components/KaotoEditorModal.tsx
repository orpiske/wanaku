import React, { useCallback, useEffect, useRef, useState } from 'react';
import { ComposedModal, ModalHeader, ModalBody, ModalFooter, Button, Loading } from '@carbon/react';

const modalStyle = `
  .kaoto-editor-modal .cds--modal-container {
    height: 95vh;
    max-height: 95vh;
    display: flex;
    flex-direction: column;
  }
  .kaoto-editor-modal .cds--modal-container .cds--modal-content {
    flex: 1;
    overflow: hidden;
    padding: 0;
    max-height: none;
  }
  .kaoto-editor-modal .cds--modal-container .cds--modal-content > div {
    height: 100%;
  }
  .kaoto-editor-modal .cds--modal-content--overflow-indicator {
    display: none;
  }
`;

interface PostMessagePayload {
  type: 'ready' | 'save' | 'load' | 'load-new' | 'request-save';
  yaml?: string;
}

interface KaotoEditorModalProps {
  open: boolean;
  yaml: string;
  onSave: (yaml: string) => void;
  onClose: () => void;
}

export const KaotoEditorModal: React.FC<KaotoEditorModalProps> = ({ open, yaml, onSave, onClose }) => {
  const iframeRef = useRef<HTMLIFrameElement>(null);
  const [isReady, setIsReady] = useState(false);
  const pendingSaveRef = useRef(false);

  useEffect(() => {
    if (!open) {
      setIsReady(false);
      pendingSaveRef.current = false;
      return;
    }

    const handleMessage = (event: MessageEvent<PostMessagePayload>) => {
      if (event.data.type === 'ready') {
        setIsReady(true);
        if (iframeRef.current?.contentWindow) {
          const payload: PostMessagePayload = yaml
            ? { type: 'load', yaml }
            : { type: 'load-new' };
          iframeRef.current.contentWindow.postMessage(payload, '*');
        }
      } else if (event.data.type === 'save' && event.data.yaml !== undefined) {
        if (pendingSaveRef.current) {
          pendingSaveRef.current = false;
          onSave(event.data.yaml);
          onClose();
        }
      }
    };

    window.addEventListener('message', handleMessage);
    return () => window.removeEventListener('message', handleMessage);
  }, [open, yaml, onSave, onClose]);

  const handleRequestSave = useCallback(() => {
    if (iframeRef.current?.contentWindow) {
      pendingSaveRef.current = true;
      iframeRef.current.contentWindow.postMessage({ type: 'request-save' }, '*');
    }
  }, []);

  return (
    <ComposedModal open={open} onClose={onClose} isFullWidth className="kaoto-editor-modal">
      <style>{modalStyle}</style>
      <ModalHeader title="Route Editor" />
      <ModalBody>
        {!isReady && (
          <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100%' }}>
            <Loading description="Loading Kaoto designer..." />
          </div>
        )}
        <iframe
          ref={iframeRef}
          src="/kaoto/"
          style={{
            width: '100%',
            height: '100%',
            border: 'none',
            display: isReady ? 'block' : 'none',
          }}
          title="Kaoto Route Editor"
        />
      </ModalBody>
      <ModalFooter>
        <Button kind="secondary" onClick={onClose}>
          Cancel
        </Button>
        <Button kind="primary" onClick={handleRequestSave} disabled={!isReady}>
          Save
        </Button>
      </ModalFooter>
    </ComposedModal>
  );
};
