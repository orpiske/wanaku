import {
  InlineNotification,
  Stack
} from "@carbon/react"
import React from "react"
import {Namespace, ToolReference} from "../../models"


interface LLMToolsProps {
  selectedNamespace: Namespace
  selectedTools: ToolReference[]
  onSelectionChange: (namespace: Namespace, tools: ToolReference[]) => void
  onError?: (message: string) => void
}

export const LLMTools: React.FC<LLMToolsProps> = () => {
  return (
    <Stack gap={5}>
      <InlineNotification
        kind="info"
        title="Tools managed by Praxis"
        subtitle="Tool selection is available through the Praxis management interface."
        lowContrast
        hideCloseButton
      />
    </Stack>
  )
}
