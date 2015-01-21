package com.betfair.aping.entities;

import com.betfair.aping.enums.InstructionReportErrorCode;
import com.betfair.aping.enums.InstructionReportStatus;

import java.util.Date;

/**
 * Created by markwilliams on 1/20/15.
 */
public class CancelInstructionReport {
    InstructionReportStatus status;
    InstructionReportErrorCode errorCode;
    CancelInstruction instruction;
    Double sizeCancelled;
    Date cancelledDate;

    public InstructionReportStatus getStatus() {
        return status;
    }

    public void setStatus(InstructionReportStatus status) {
        this.status = status;
    }

    public InstructionReportErrorCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(InstructionReportErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public CancelInstruction getInstruction() {
        return instruction;
    }

    public void setInstruction(CancelInstruction instruction) {
        this.instruction = instruction;
    }

    public Double getSizeCancelled() {
        return sizeCancelled;
    }

    public void setSizeCancelled(Double sizeCancelled) {
        this.sizeCancelled = sizeCancelled;
    }

    public Date getCancelledDate() {
        return cancelledDate;
    }

    public void setCancelledDate(Date cancelledDate) {
        this.cancelledDate = cancelledDate;
    }
}
