package com.freightos.fms.application.masterbl;

import com.freightos.common.exception.ResourceNotFoundException;
import com.freightos.fms.application.attachment.port.in.BlAttachmentUseCase;
import com.freightos.fms.application.masterbl.command.ChangeMasterBlNoCommand;
import com.freightos.fms.application.housebl.port.out.HouseBlPort;
import com.freightos.fms.application.masterbl.port.out.MasterBlPort;
import com.freightos.fms.application.masterbl.port.out.SeaMasterPersistencePort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class MasterBlServiceChangeMblNoTest {

    @Mock private MasterBlPort masterBlPort;
    @Mock private SeaMasterPersistencePort seaMasterPersistencePort;
    @Mock private HouseBlPort houseBlPort;
    @Mock private MasterBlFactory masterBlFactory;
    @Mock private BlAttachmentUseCase blAttachmentUseCase;

    @InjectMocks
    private MasterBlService masterBlService;

    @Test
    @DisplayName("changeMblNo - consoled house_bl 0건일 때 master_bl만 UPDATE 성공")
    void changeMblNo_noConsoledHouseBl_updatesMasterOnly() {
        Long id = 1L;
        ChangeMasterBlNoCommand cmd = new ChangeMasterBlNoCommand("NEWMBL001", "NEWREF001");
        given(houseBlPort.updateMasterRefByMasterBlId(id, "NEWMBL001", "NEWREF001")).willReturn(0);
        given(masterBlPort.updateMblNoAndMasterRefById(id, "NEWMBL001", "NEWREF001")).willReturn(1L);

        masterBlService.changeMblNo(id, cmd);

        then(houseBlPort).should().updateMasterRefByMasterBlId(id, "NEWMBL001", "NEWREF001");
        then(masterBlPort).should().updateMblNoAndMasterRefById(id, "NEWMBL001", "NEWREF001");
    }

    @Test
    @DisplayName("changeMblNo - consoled house_bl N건일 때 house_bl bulk UPDATE 후 master_bl UPDATE")
    void changeMblNo_withConsoledHouseBls_updatesBothInOrder() {
        Long id = 2L;
        ChangeMasterBlNoCommand cmd = new ChangeMasterBlNoCommand("NEWMBL002", "NEWREF002");
        given(houseBlPort.updateMasterRefByMasterBlId(id, "NEWMBL002", "NEWREF002")).willReturn(3);
        given(masterBlPort.updateMblNoAndMasterRefById(id, "NEWMBL002", "NEWREF002")).willReturn(1L);

        masterBlService.changeMblNo(id, cmd);

        then(houseBlPort).should().updateMasterRefByMasterBlId(id, "NEWMBL002", "NEWREF002");
        then(masterBlPort).should().updateMblNoAndMasterRefById(id, "NEWMBL002", "NEWREF002");
    }

    @Test
    @DisplayName("changeMblNo - master_bl 미존재(affected=0) 시 ResourceNotFoundException, house_bl는 이미 UPDATE됨")
    void changeMblNo_masterNotFound_throwsResourceNotFoundException() {
        Long id = 999L;
        ChangeMasterBlNoCommand cmd = new ChangeMasterBlNoCommand("MBL-GHOST", "REF-GHOST");
        given(houseBlPort.updateMasterRefByMasterBlId(id, "MBL-GHOST", "REF-GHOST")).willReturn(0);
        given(masterBlPort.updateMblNoAndMasterRefById(id, "MBL-GHOST", "REF-GHOST")).willReturn(0L);

        assertThatThrownBy(() -> masterBlService.changeMblNo(id, cmd))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("changeMblNo - mblNo blank 시 IllegalArgumentException, port 미호출")
    void changeMblNo_blankMblNo_throwsIllegalArgumentException() {
        Long id = 1L;
        ChangeMasterBlNoCommand cmd = new ChangeMasterBlNoCommand("   ", "NEWREF001");

        assertThatThrownBy(() -> masterBlService.changeMblNo(id, cmd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mblNo");

        then(houseBlPort).should(never()).updateMasterRefByMasterBlId(id, "   ", "NEWREF001");
        then(masterBlPort).should(never()).updateMblNoAndMasterRefById(id, "   ", "NEWREF001");
    }

    @Test
    @DisplayName("changeMblNo - masterRefNo blank 시 IllegalArgumentException, port 미호출")
    void changeMblNo_blankMasterRefNo_throwsIllegalArgumentException() {
        Long id = 1L;
        ChangeMasterBlNoCommand cmd = new ChangeMasterBlNoCommand("NEWMBL001", "");

        assertThatThrownBy(() -> masterBlService.changeMblNo(id, cmd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("masterRefNo");

        then(houseBlPort).should(never()).updateMasterRefByMasterBlId(id, "NEWMBL001", "");
        then(masterBlPort).should(never()).updateMblNoAndMasterRefById(id, "NEWMBL001", "");
    }
}
