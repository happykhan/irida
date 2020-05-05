import React, { useEffect, useReducer } from "react";
import { PageWrapper } from "../../components/page/PageWrapper";
import { useNavigate } from "@reach/router";
import { getUserGroupDetails } from "../../apis/users/groups";
import { Table, Typography } from "antd";

const { Paragraph } = Typography;

function UserGroupMembersTable({ members }) {
  const columns = [
    {
      dataIndex: "name",
      title: "Member Name",
    },
  ];

  return <Table columns={columns} dataSource={members} />;
}

const reducer = (state, action) => {
  switch (action.type) {
    case "load":
      return { ...state, loading: false, ...action.payload };
    default:
      return { ...state };
  }
};

export function UserGroupDetails({ id }) {
  const [state, dispatch] = useReducer(reducer, { loading: true });

  const navigate = useNavigate();

  useEffect(() => {
    getUserGroupDetails(id).then((response) =>
      dispatch({ type: "load", payload: response })
    );
  }, [id]);

  return (
    <PageWrapper
      title={"User Groups"}
      onBack={() => navigate("/groups", { replace: true })}
    >
      <Paragraph editable>{state.name}</Paragraph>
      <Paragraph editable>{state.description}</Paragraph>
      <UserGroupMembersTable members={state.members} />
    </PageWrapper>
  );
}
