package br.com.nhac.backend_nhac.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class TwilioSmsServiceTest {

    @InjectMocks
    private TwilioSmsService twilioSmsService;

    @BeforeEach
    void setUp() {
        // Defaults to mock mode for safety
        ReflectionTestUtils.setField(twilioSmsService, "mockMode", true);
        ReflectionTestUtils.setField(twilioSmsService, "accountSid", "mockSid");
        ReflectionTestUtils.setField(twilioSmsService, "authToken", "mockToken");
        ReflectionTestUtils.setField(twilioSmsService, "fromNumber", "+1234567890");
    }

    @Test
    void init_ComMockModeTrue_DeveOperarEmMock() {
        assertDoesNotThrow(() -> twilioSmsService.init());
    }

    @Test
    void init_ComMockModeFalse_InicializaTwilio() {
        ReflectionTestUtils.setField(twilioSmsService, "mockMode", false);
        assertDoesNotThrow(() -> twilioSmsService.init());
    }

    @Test
    void enviarSms_EmMockMode_ApenasLoga() {
        assertDoesNotThrow(() -> twilioSmsService.enviarSms("+5511999999999", "Teste de mensagem mock"));
    }

    @Test
    void enviarSms_Real_DeveLancarExcecaoSemInternetOuCredenciaisInvalidas() {
        ReflectionTestUtils.setField(twilioSmsService, "mockMode", false);
        
        assertThrows(RuntimeException.class, () -> {
            twilioSmsService.enviarSms("+5511999999999", "Teste de mensagem real");
        });
    }
}
