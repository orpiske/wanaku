import {SideNav, SideNavItems, SideNavMenu, SideNavMenuItem,} from '@carbon/react';
import {Links} from "../router/links.models";
import {Link} from 'react-router-dom';

interface SideNavComponentProps {
    isSideNavExpanded: boolean;
    onClickSideNavExpand: () => void;
}

function SideNavComponent({ isSideNavExpanded, onClickSideNavExpand }:SideNavComponentProps) {
    return (
        <SideNav
            aria-label="Side navigation"
            expanded={isSideNavExpanded}
            isPersistent={false}
            onOverlayClick={onClickSideNavExpand}
        >
            <SideNavItems>
                <SideNavMenuItem
                    element={Link}
                    to="/home"
                    onClick={onClickSideNavExpand}
                >
                    Home
                </SideNavMenuItem>
                <SideNavMenu title="Developer">
                    <SideNavMenuItem
                        element={Link}
                        to={Links.LLMChat}
                        onClick={onClickSideNavExpand}
                    >
                        LLMChat
                    </SideNavMenuItem>
                    <SideNavMenuItem
                        element={Link}
                        to={Links.CodeExecution}
                        onClick={onClickSideNavExpand}
                    >
                        Code Execution
                    </SideNavMenuItem>
                    <SideNavMenuItem
                        element={Link}
                        to={Links.ToolCalls}
                        onClick={onClickSideNavExpand}
                    >
                        Tool Call Debugger
                    </SideNavMenuItem>
                    <SideNavMenuItem
                        element={Link}
                        to={Links.DataStores}
                        onClick={onClickSideNavExpand}
                    >
                        Data Stores
                    </SideNavMenuItem>
                </SideNavMenu>
                <SideNavMenuItem
                    element={Link}
                    to="/capabilities"
                    onClick={onClickSideNavExpand}
                >
                    Capabilities
                </SideNavMenuItem>
                <SideNavMenuItem
                    element={Link}
                    to={Links.ServiceCatalog}
                    onClick={onClickSideNavExpand}
                >
                    Service Catalog
                </SideNavMenuItem>
            </SideNavItems>
        </SideNav>
    );
}

export default SideNavComponent;
