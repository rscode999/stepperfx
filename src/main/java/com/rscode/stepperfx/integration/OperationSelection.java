package com.rscode.stepperfx.integration;

/**
 * Possible user preferences for algorithm and forward/reverse operation. Example: Stepper 2, encrypt.
 */
public enum OperationSelection {
    STEPPER_ENCRYPT,
    STEPPER_DECRYPT,
    STEPPER2_ENCRYPT,
    STEPPER2_DECRYPT;

    /**
     * Returns whether this operation encrypts its input (as opposed to decrypting the input)
     * @return true if operation is encryption
     */
    public boolean isEncryptionOperation() {
        return this.equals(STEPPER_ENCRYPT) || this.equals(STEPPER2_ENCRYPT);
    }


    /**
     * Returns whether this operation carries out a Stepper 2 operation.<br><br>
     *
     * This method will likely be replaced if more operations are added to the app.
     * @return true if operation is Stepper 2
     */
    public boolean isStepper2Operation() {
        return this.equals(STEPPER2_ENCRYPT) || this.equals(STEPPER2_DECRYPT);
    }
}
