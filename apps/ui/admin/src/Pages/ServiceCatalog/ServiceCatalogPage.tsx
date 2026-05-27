import React, {useCallback, useEffect, useState} from "react";
import {Button, Tab, TabList, TabPanel, TabPanels, Tabs, ToastNotification} from "@carbon/react";
import {Add} from "@carbon/icons-react";
import {ServiceCatalogCards} from "./ServiceCatalogCards";
import {ServiceTemplateCards} from "./ServiceTemplateCards";
import {ToolsetReposTab} from "./ToolsetReposTab";
import {KaotoEditorModal} from "../../components/KaotoEditorModal";
import {useServiceCatalog} from "../../hooks/api/use-service-catalog";
import {useServiceTemplate} from "../../hooks/api/use-service-template";
import "./ServiceCatalogPage.scss";

interface ServiceCatalogSystem {
  name: string;
  routesFile: string;
  rulesFile: string;
  dependenciesFile?: string;
}

interface ServiceCatalogDetail {
  id: string;
  name: string;
  icon?: string;
  description: string;
  services: ServiceCatalogSystem[];
}

interface ServiceCatalogSummary {
  id: string;
  name: string;
  icon?: string;
  description: string;
  services: string[];
}

interface ServiceTemplateSummary {
  id: string;
  name: string;
  icon?: string;
  description: string;
  services: string[];
  hasProperties?: boolean;
}

export const ServiceCatalogPage: React.FC = () => {
  const [catalogs, setCatalogs] = useState<ServiceCatalogSummary[]>([]);
  const [templates, setTemplates] = useState<ServiceTemplateSummary[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isLoadingTemplates, setIsLoadingTemplates] = useState(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [createEditorOpen, setCreateEditorOpen] = useState(false);
  const { listServiceCatalogs, getServiceCatalog, removeServiceCatalog, getRouteYaml, updateRouteYaml } = useServiceCatalog();
  const { listServiceTemplates } = useServiceTemplate();

  const fetchCatalogs = useCallback(
    async (search?: string) => {
      try {
        const result = await listServiceCatalogs(search);
        const body = result.data as { data: ServiceCatalogSummary[] };
        setCatalogs(body.data || []);
        setIsLoading(false);
      } catch (error) {
        console.error("Error fetching service catalogs:", error);
        setErrorMessage("Failed to load service catalogs");
        setIsLoading(false);
      }
    },
    [listServiceCatalogs]
  );

  const fetchTemplates = useCallback(
    async (search?: string) => {
      try {
        const result = await listServiceTemplates(search);
        const body = result.data as { data: ServiceTemplateSummary[] };
        setTemplates(body.data || []);
        setIsLoadingTemplates(false);
      } catch (error) {
        console.error("Error fetching service templates:", error);
        setErrorMessage("Failed to load service templates");
        setIsLoadingTemplates(false);
      }
    },
    [listServiceTemplates]
  );

  useEffect(() => {
    fetchCatalogs();
    fetchTemplates();
  }, [fetchCatalogs, fetchTemplates]);

  useEffect(() => {
    if (errorMessage) {
      const timer = setTimeout(() => setErrorMessage(null), 10000);
      return () => clearTimeout(timer);
    }
  }, [errorMessage]);

  useEffect(() => {
    if (successMessage) {
      const timer = setTimeout(() => setSuccessMessage(null), 5000);
      return () => clearTimeout(timer);
    }
  }, [successMessage]);

  const handleDelete = async (name: string) => {
    try {
      await removeServiceCatalog(name);
      setSuccessMessage(`Service catalog '${name}' deleted successfully`);
      fetchCatalogs();
    } catch (error) {
      console.error("Error deleting service catalog:", error);
      setErrorMessage(`Failed to delete service catalog '${name}'`);
    }
  };

  const handleSearch = (search: string) => {
    fetchCatalogs(search || undefined);
  };

  const handleTemplateSearch = (search: string) => {
    fetchTemplates(search || undefined);
  };

  const handleGetDetail = async (name: string): Promise<ServiceCatalogDetail | null> => {
    try {
      const result = await getServiceCatalog(name);
      const body = result.data as { data: ServiceCatalogDetail };
      return body.data;
    } catch (error) {
      console.error("Error fetching catalog detail:", error);
      return null;
    }
  };

  const handleInstantiateSuccess = (templateName: string) => {
    setSuccessMessage(`Service catalog created successfully from template '${templateName}'`);
    fetchCatalogs();
  };

  const handleGetRouteYaml = async (name: string, system: string): Promise<string> => {
    try {
      const result = await getRouteYaml(name, system);
      const body = result.data as { data: string };
      return body.data;
    } catch (error) {
      console.error("Error fetching route YAML:", error);
      throw error;
    }
  };

  const handleUpdateRouteYaml = async (name: string, system: string, yaml: string): Promise<void> => {
    try {
      await updateRouteYaml(name, system, yaml);
      setSuccessMessage(`Route updated successfully for ${system}`);
    } catch (error) {
      console.error("Error updating route YAML:", error);
      setErrorMessage(`Failed to update route for ${system}`);
      throw error;
    }
  };

  const handleCreateCatalog = (_yaml: string) => {
    setSuccessMessage("Route designed successfully. Use the CLI to package and deploy it as a catalog.");
    setCreateEditorOpen(false);
  };

  return (
    <div className="service-catalog-page">
      {errorMessage && (
        <ToastNotification
          kind="error"
          title="Error"
          subtitle={errorMessage}
          onCloseButtonClick={() => setErrorMessage(null)}
          timeout={10000}
          style={{ float: "right" }}
        />
      )}
      {successMessage && (
        <ToastNotification
          kind="success"
          title="Success"
          subtitle={successMessage}
          onCloseButtonClick={() => setSuccessMessage(null)}
          timeout={5000}
          style={{ float: "right" }}
        />
      )}
      <div className="service-catalog-header">
        <div>
          <h1 className="title">Service Catalog</h1>
          <p className="description">
            View and manage deployed service catalogs, service templates, and remote toolset repositories.
          </p>
        </div>
        <Button
          kind="primary"
          renderIcon={Add}
          onClick={() => setCreateEditorOpen(true)}
        >
          Create Catalog
        </Button>
      </div>
      <Tabs>
        <TabList aria-label="Service catalog tabs">
          <Tab>Service Catalogs</Tab>
          <Tab>Service Templates</Tab>
          <Tab>Toolset Repositories</Tab>
        </TabList>
        <TabPanels>
          <TabPanel>
            {isLoading ? (
              <div>Loading...</div>
            ) : (
              <ServiceCatalogCards
                catalogs={catalogs}
                onDelete={handleDelete}
                onSearch={handleSearch}
                getDetail={handleGetDetail}
                getRouteYaml={handleGetRouteYaml}
                updateRouteYaml={handleUpdateRouteYaml}
              />
            )}
          </TabPanel>
          <TabPanel>
            {isLoadingTemplates ? (
              <div>Loading...</div>
            ) : (
              <ServiceTemplateCards
                templates={templates}
                onSearch={handleTemplateSearch}
                onInstantiateSuccess={handleInstantiateSuccess}
              />
            )}
          </TabPanel>
          <TabPanel>
            <ToolsetReposTab
              onError={(msg) => setErrorMessage(msg)}
              onSuccess={(msg) => setSuccessMessage(msg)}
            />
          </TabPanel>
        </TabPanels>
      </Tabs>

      {createEditorOpen && (
        <KaotoEditorModal
          open={createEditorOpen}
          yaml=""
          onSave={handleCreateCatalog}
          onClose={() => setCreateEditorOpen(false)}
        />
      )}
    </div>
  );
};
