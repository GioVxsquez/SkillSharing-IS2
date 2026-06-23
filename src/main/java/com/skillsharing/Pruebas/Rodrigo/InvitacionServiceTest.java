@Test
void enviarInvitacion() {

    when(sesionRepository.findById(10L))
            .thenReturn(Optional.of(sesion));

    when(usuarioRepository.findById(2L))
            .thenReturn(Optional.of(invitado));

    when(invitacionRepository.existsBySesionSesionIdAndInvitadoUsuarioId(10L,2L))
            .thenReturn(false);

    when(invitacionRepository.countBySesionSesionIdAndEstadoIn(
            eq(10L),
            anyList()))
            .thenReturn(0L);

    when(invitacionRepository.save(any(Invitacion.class)))
            .thenAnswer(i -> i.getArgument(0));

    Invitacion resultado =
            invitacionService.enviarInvitacion(1L,10L,2L);

    assertEquals(EstadoInvitacion.ACEPTADA, resultado.getEstado());
}