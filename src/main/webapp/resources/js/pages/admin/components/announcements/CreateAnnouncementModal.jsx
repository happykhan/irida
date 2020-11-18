import React, { useRef } from "react";
import { Checkbox, Form, Input, Modal } from "antd";
import { IconEdit } from "../../../../components/icons/Icons";
import { FONT_COLOR_PRIMARY } from "../../../../styles/fonts";
import { MarkdownEditor } from "../../../../components/markdown/MarkdownEditor";

/**
 * React component to display an Ant Design Modal containing a form to create a new announcement
 * @param {boolean} visible - whether the modal is open or not
 * @param {function} closeModal - the function to close the modal
 * @param {function} createAnnouncement - the function to create a new announcement and refresh the table
 * @returns {JSX.Element}
 * @constructor
 */
export function CreateAnnouncementModal({
  visible,
  closeModal,
  createAnnouncement,
}) {
  const markdownRef = useRef();
  const [form] = Form.useForm();

  const onCancel = () => {
    form.resetFields();
    closeModal();
  };

  function saveMarkdown() {
    form.validateFields().then((values) => {
      const markdown = markdownRef.current.getMarkdown();
      const title = values.title;
      const priority = values.priority;

      createAnnouncement(title, markdown, priority);
      closeModal();
    });
  }
  return (
    <Modal
      visible={visible}
      title={
        <>
          <IconEdit style={{ color: FONT_COLOR_PRIMARY }} />
          {i18n("CreateNewAnnouncement.title")}
        </>
      }
      width="80%"
      okText={i18n("CreateNewAnnouncement.okBtn")}
      okButtonProps={{
        className: "t-submit-announcement",
      }}
      onOk={saveMarkdown}
      onCancel={onCancel}
    >
      <Form layout="vertical" form={form}>
        <Form.Item
          name="title"
          label={i18n("announcement.control.title")}
          rules={[
            {
              required: true,
              message: i18n("CreateNewAnnouncement.form.title"),
            },
          ]}
        >
          <Input />
        </Form.Item>
        <Form.Item name="message" label={i18n("announcement.control.message")}>
          <MarkdownEditor ref={markdownRef} />
        </Form.Item>
        <Form.Item
          name="priority"
          label={i18n("announcement.control.priority")}
          valuePropName="checked"
        >
          <Checkbox />
        </Form.Item>
      </Form>
    </Modal>
  );
}
