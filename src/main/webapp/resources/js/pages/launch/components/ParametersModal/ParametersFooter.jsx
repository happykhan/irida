import React from "react";
import {
  Alert,
  Button,
  Dropdown,
  Form,
  Input,
  Menu,
  Space,
  Tooltip,
} from "antd";
import { grey9, yellow6 } from "../../../../styles/colors";
import { IconExclamationCircle } from "../../../../components/icons/Icons";
import { SPACE_SM, SPACE_XS } from "../../../../styles/spacing";
import { useLaunchState } from "../../launch-context";
import { SaveParametersAsFooter } from "./SaveParametersAsFooter";

export function ParametersFooter({
  modified,
  onCancel,
  saveModifiedParameters,
  onSaveAs,
}) {
  const { parameterSet } = useLaunchState();

  /*
  Determines if the save form should be displayed.
   */
  const [showSave, setShowSave] = React.useState(false);

  /*
  Determines if the save as... form should be displayed
   */
  const [showSaveAs, setShowSaveAs] = React.useState(false);

  const menu = (
    <Menu>
      {parameterSet.id !== 0 ? (
        <Menu.Item key="copy" onClick={() => setShowSave(true)}>
          {i18n("SavedParameters.save")}
        </Menu.Item>
      ) : null}
      <Menu.Item key="save" onClick={() => setShowSaveAs(true)}>
        {i18n("SavedParameters.saveAs")}
      </Menu.Item>
    </Menu>
  );

  return (
    <Space style={{ width: `100%` }} direction="vertical">
      {showSaveAs || showSave ? null : (
        <div style={{ display: "flex", flexDirection: "row-reverse" }}>
          <Space>
            <Button onClick={onCancel}>Cancel</Button>
            {modified ? (
              <Dropdown.Button
                type="primary"
                overlay={menu}
                buttonsRender={([leftButton, rightButton]) => [
                  <Tooltip
                    color={yellow6}
                    title={
                      <span style={{ color: grey9 }}>
                        <IconExclamationCircle
                          style={{ marginRight: SPACE_XS }}
                        />
                        {i18n("SavedParameters.useModified.tooltip")}
                      </span>
                    }
                    key="leftButton"
                  >
                    {React.cloneElement(leftButton, {
                      onClick: saveModifiedParameters,
                    })}
                  </Tooltip>,
                  rightButton,
                ]}
              >
                {i18n("SavedParameters.useModified.btn")}
              </Dropdown.Button>
            ) : (
              <Button type="primary" disabled>
                {i18n("SavedParameters.useModified.btn")}
              </Button>
            )}
          </Space>
        </div>
      )}
      {showSaveAs ? (
        <SaveParametersAsFooter
          onCancel={() => setShowSaveAs(false)}
          onSaveAs={onSaveAs}
        />
      ) : null}
      {showSave ? (
        <Form layout="vertical">
          <Alert
            style={{ textAlign: "left", marginBottom: SPACE_SM }}
            type="warning"
            showIcon
            message="This will overwrite the current parameter set"
            description={
              "EVerytihng else will magically be the same. Have fun and do evil things."
            }
          />
          <Button onClick={() => setShowSave(false)}>Cancel</Button>
          <Button onClick={() => overwriteParameterSet()}>Save</Button>
        </Form>
      ) : null}
    </Space>
  );
}
