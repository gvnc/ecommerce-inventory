import React, { Component } from 'react'
import {Dialog} from "primereact/dialog";
import {Button} from "primereact/button";

export default class ConfirmationDialog extends Component {

    render() {
        let dialogFooter =
            <div className="ui-dialog-buttonpane p-clearfix">
                <Button label="Yes" icon="pi pi-check" className="contentsMarginRight"
                        onClick={this.props.yesHandler} />
                <Button label="No" icon="pi pi-times" className="contentsMarginRight"
                        onClick={this.props.noHandler} />
            </div>;

        return (
            <Dialog visible={this.props.visibleProperty} header="Confirmation" modal={true}
                    footer={dialogFooter} onHide={this.props.noHandler}>
                <p>{this.props.message}</p>
            </Dialog>
        )
    }
}