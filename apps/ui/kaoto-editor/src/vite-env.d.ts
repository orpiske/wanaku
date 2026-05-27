/// <reference types="vite/client" />

declare module '*.css';

declare module '@kaoto/kaoto/lib/providers' {
  import { FC, PropsWithChildren } from 'react';
  export const EntitiesContext: React.Context<any>;
  export const EntitiesProvider: FC<PropsWithChildren<{ fileExtension?: string }>>;
  export const ReloadProvider: FC<PropsWithChildren>;
  export const RuntimeProvider: FC<PropsWithChildren<{ catalogUrl: string }>>;
  export const SchemasLoaderProvider: FC<PropsWithChildren>;
  export const VisibleFlowsContext: React.Context<any>;
  export const VisibleFlowsProvider: FC<PropsWithChildren>;
}

declare module '@kaoto/kaoto/lib/dynamic-catalog/catalog.provider' {
  import { FC, PropsWithChildren } from 'react';
  export const CatalogLoaderProvider: FC<PropsWithChildren>;
}

declare module '@kaoto/kaoto/lib/components/Visualization/Canvas/controller.service' {
  export class ControllerService {
    static createController(): any;
  }
}

declare module '@kaoto/kaoto/lib/utils' {
  export class EventNotifier extends EventTarget {
    static getInstance(): EventNotifier;
    next(event: string, payload?: any): void;
    subscribe(event: string, listener: (detail: any) => void): () => void;
  }
}
