package edu.gladstone.hts.microscope;

public interface MicroscopeCommandHandler{
    void HandleRetractCommand();

    void HandleExtendCommand();

    String HandleGetStatusCommand();

    void HandleImageCommand(String Data);

    void HandleAbortCommand();
    
}
