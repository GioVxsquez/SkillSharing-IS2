@Test
void aceptarInvitacion() {

    when(invitacionRepository.findById(20L))
            .thenReturn(Optional.of(invitacion));

    when(invitacionRepository.save(any()))
            .thenAnswer(i -> i.getArgument(0));

    invitacionService.responderInvitacion(
            2L,
            20L,
            true
    );

    assertEquals(
            EstadoInvitacion.RECHAZADA,
            invitacion.getEstado()
    );
}