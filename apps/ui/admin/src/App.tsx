import {HeaderContainer} from "@carbon/react";
import "./App.scss";

import Header from "./components/Header";
import SideNav from "./components/SideNav";
import Content from "./components/Content";

function App() {

  return (
    <HeaderContainer
      render={({ isSideNavExpanded, onClickSideNavExpand }) => (
        <>
          <Header isSideNavExpanded={isSideNavExpanded} onClickSideNavExpand={onClickSideNavExpand}/>
          <SideNav isSideNavExpanded={isSideNavExpanded} onClickSideNavExpand={onClickSideNavExpand}/>
          <Content/>
        </>
      )}
    />
  );
}

export default App;
