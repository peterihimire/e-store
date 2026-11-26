package com.benkih.estore.refund.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.benkih.estore.common.enums.RefundStatus;
import com.benkih.estore.common.exceptions.ForbiddenException;
import com.benkih.estore.common.exceptions.PaymentException;
import com.benkih.estore.order.entity.Order;
import com.benkih.estore.order.repository.OrderItemRepository;
import com.benkih.estore.order.repository.OrderRepository;
import com.benkih.estore.payment.repository.PaymentRepository;
import com.benkih.estore.refund.dto.request.CreateRefundRequest;
import com.benkih.estore.refund.dto.response.RefundResponse;
import com.benkih.estore.refund.entity.Refund;
import com.benkih.estore.refund.repository.RefundItemRepository;
import com.benkih.estore.refund.repository.RefundRepository;
import com.benkih.estore.security.user.CurrentUserService;
import com.benkih.estore.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RefundApprovalServiceTest {

    @Mock private CurrentUserService currentUserService;
    @Mock private RefundRepository refundRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private RefundItemRepository refundItemRepository;
    @Mock private RefundQueryService refundQueryService;

    @InjectMocks private RefundApprovalService service;

    @Test
    void approveMovesRequestedRefundToApproved() {
        Refund refund = refund(RefundStatus.REQUESTED);
        RefundResponse response = RefundResponse.builder().build();

        when(refundQueryService.getRefundEntity("refund-slug")).thenReturn(refund);
        when(refundRepository.save(refund)).thenReturn(refund);
        when(refundQueryService.convertToDto(refund)).thenReturn(response);

        assertEquals(response, service.approveRefund("refund-slug"));
        assertEquals(RefundStatus.APPROVED, refund.getRefundStatus());
        verify(refundRepository).save(refund);
    }

    @Test
    void rejectRecordsFailureReason() {
        Refund refund = refund(RefundStatus.REQUESTED);
        RefundResponse response = RefundResponse.builder().build();

        when(refundQueryService.getRefundEntity("refund-slug")).thenReturn(refund);
        when(refundRepository.save(refund)).thenReturn(refund);
        when(refundQueryService.convertToDto(refund)).thenReturn(response);

        assertEquals(response, service.rejectRefund("refund-slug", "Out of stock"));
        assertEquals(RefundStatus.REJECTED, refund.getRefundStatus());
        assertEquals("Out of stock", refund.getFailureReason());
    }

    @Test
    void rejectsApprovalOfAlreadyApprovedRefund() {
        Refund refund = refund(RefundStatus.APPROVED);
        when(refundQueryService.getRefundEntity("refund-slug")).thenReturn(refund);

        assertThrows(PaymentException.class, () -> service.approveRefund("refund-slug"));

        verify(refundRepository, never()).save(org.mockito.ArgumentMatchers.any(Refund.class));
    }

    @Test
    void requestRefundRejectsAnotherUsersOrder() {
        User currentUser = user(1L);
        User orderOwner = user(2L);
        Order order = new Order();
        order.setSlug("order-slug");
        order.setUser(orderOwner);

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(orderRepository.findBySlug("order-slug")).thenReturn(Optional.of(order));

        CreateRefundRequest request =
                new CreateRefundRequest("order-slug", "Changed my mind", List.of());

        assertThrows(ForbiddenException.class, () -> service.requestRefund(request));

        verify(refundRepository, never()).save(org.mockito.ArgumentMatchers.any(Refund.class));
    }

    private Refund refund(RefundStatus status) {
        Refund refund = new Refund();
        refund.setSlug("refund-slug");
        refund.setRefundStatus(status);
        return refund;
    }

    private User user(Long id) {
        User user = new User();
        user.setId(id);
        return user;
    }
}
