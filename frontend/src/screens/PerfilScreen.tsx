import React, { useEffect, useState, useCallback } from 'react';
import {
  View, Text, ScrollView, StyleSheet, TouchableOpacity,
  ActivityIndicator, Alert, StatusBar, RefreshControl,
} from 'react-native';
import { api } from '../api/config';

// hu23: visualizar mi perfil
// US21/US22: Gestión de habilidades del instructor
export default function PerfilScreen({ navigation }: any) {
  const [perfil, setPerfil]           = useState<any>(null);
  const [habilidades, setHabilidades] = useState<any[]>([]);
  const [misHabilidades, setMisHab]   = useState<number[]>([]);
  const [loading, setLoading]         = useState(true);
  const [guardando, setGuardando]     = useState(false);
  const [refreshing, setRefreshing]   = useState(false);

  const cargar = useCallback(async () => {
    try {
      const [respPerfil, respHabilidades] = await Promise.all([
        api.get('/usuarios/me'),
        api.get('/habilidades'),
      ]);
      const p = respPerfil.data.data;
      setPerfil(p);
      setHabilidades(respHabilidades.data.data || []);
      // Pre-seleccionar las habilidades del perfil actual
      const idsActuales = (p.habilidades || []).map((h: any) => h.habilidadId);
      setMisHab(idsActuales);
    } catch {
      Alert.alert('Error', 'No se pudo cargar el perfil.');
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, []);

  useEffect(() => { cargar(); }, [cargar]);

  const toggleHabilidad = (id: number) => {
    setMisHab((prev) =>
      prev.includes(id) ? prev.filter((h) => h !== id) : [...prev, id]
    );
  };

  const guardarHabilidades = async () => {
    setGuardando(true);
    try {
      await api.put('/habilidades/mis-habilidades', misHabilidades);
      Alert.alert('Guardado', 'Tus habilidades fueron actualizadas.');
    } catch (error: any) {
      Alert.alert('Error', error.response?.data?.mensaje || 'No se pudieron guardar las habilidades.');
    } finally {
      setGuardando(false);
    }
  };

  if (loading) {
    return <ActivityIndicator style={{ flex: 1 }} size="large" color="#1B3A6B" />;
  }

  return (
    <View style={styles.container}>
      <StatusBar barStyle="light-content" backgroundColor="#1B3A6B" />
      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()}>
          <Text style={styles.backTexto}>Volver</Text>
        </TouchableOpacity>
        <Text style={styles.headerTitulo}>Mi Perfil</Text>
      </View>

      <ScrollView
        contentContainerStyle={styles.scroll}
        refreshControl={
          <RefreshControl refreshing={refreshing} onRefresh={() => { setRefreshing(true); cargar(); }} colors={['#1B3A6B']} />
        }
      >
        {/* Avatar y datos principales */}
        <View style={styles.avatarCard}>
          <View style={styles.avatar}>
            <Text style={styles.avatarTexto}>{perfil?.nombre?.charAt(0)?.toUpperCase() || '?'}</Text>
          </View>
          <Text style={styles.nombre}>{perfil?.nombre}</Text>
          <Text style={styles.email}>{perfil?.email}</Text>
          <View style={styles.rolBadge}>
            <Text style={styles.rolTexto}>
              {perfil?.rol === 'INSTRUCTOR' ? '🏫 Instructor' : '📚 Aprendiz'}
            </Text>
          </View>
          <Text style={styles.fechaRegistro}>
            Miembro desde: {perfil?.fechaRegistro ? new Date(perfil.fechaRegistro).toLocaleDateString('es-PE') : '-'}
          </Text>
        </View>

        {/* Datos de cuenta */}
        <View style={styles.seccion}>
          <Text style={styles.seccionTitulo}>Datos de cuenta</Text>
          <View style={styles.fila}>
            <Text style={styles.filaLabel}>Nombre</Text>
            <Text style={styles.filaValor}>{perfil?.nombre || '-'}</Text>
          </View>
          <View style={styles.fila}>
            <Text style={styles.filaLabel}>Correo</Text>
            <Text style={styles.filaValor}>{perfil?.email || '-'}</Text>
          </View>
          <View style={styles.fila}>
            <Text style={styles.filaLabel}>Rol</Text>
            <Text style={styles.filaValor}>{perfil?.rol || '-'}</Text>
          </View>
        </View>

        {/* Sección de habilidades - US21 */}
        <View style={styles.seccion}>
          <Text style={styles.seccionTitulo}>🎯 Mis Habilidades</Text>
          <Text style={styles.habilidadesHint}>Selecciona las habilidades que dominas o que te interesan:</Text>
          <View style={styles.habilidadesGrid}>
            {habilidades.map((h) => {
              const seleccionada = misHabilidades.includes(h.habilidadId);
              return (
                <TouchableOpacity
                  key={h.habilidadId}
                  style={[styles.habilidadChip, seleccionada && styles.habilidadChipActiva]}
                  onPress={() => toggleHabilidad(h.habilidadId)}
                >
                  <Text style={[styles.habilidadChipTexto, seleccionada && styles.habilidadChipTextoActivo]}>
                    {seleccionada ? '✓ ' : ''}{h.nombre}
                  </Text>
                </TouchableOpacity>
              );
            })}
          </View>
          <TouchableOpacity
            style={[styles.guardarBtn, guardando && { opacity: 0.7 }]}
            onPress={guardarHabilidades}
            disabled={guardando}
          >
            {guardando
              ? <ActivityIndicator color="#fff" size="small" />
              : <Text style={styles.guardarBtnTexto}>Guardar habilidades</Text>}
          </TouchableOpacity>
        </View>

        {/* Acciones */}
        <TouchableOpacity style={styles.botonInvitaciones} onPress={() => navigation.navigate('Invitaciones')}>
          <Text style={styles.botonInvitacionesTexto}>📩 Ver mis invitaciones</Text>
        </TouchableOpacity>

        <TouchableOpacity style={[styles.botonInvitaciones, { marginTop: 10, borderColor: '#38A169' }]} onPress={() => navigation.navigate('Notificaciones')}>
          <Text style={[styles.botonInvitacionesTexto, { color: '#38A169' }]}>🔔 Ver notificaciones</Text>
        </TouchableOpacity>
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#F7F9FC' },
  header: { backgroundColor: '#1B3A6B', paddingTop: 48, paddingBottom: 16, paddingHorizontal: 20 },
  backTexto: { color: '#C8D8F0', fontSize: 14, marginBottom: 6 },
  headerTitulo: { color: '#FFFFFF', fontSize: 20, fontWeight: '700' },
  scroll: { padding: 16, paddingBottom: 40 },
  avatarCard: {
    backgroundColor: '#FFFFFF', borderRadius: 12, padding: 24, alignItems: 'center', marginBottom: 16,
    shadowColor: '#000', shadowOffset: { width: 0, height: 2 }, shadowOpacity: 0.07, shadowRadius: 6, elevation: 3,
  },
  avatar: { width: 80, height: 80, borderRadius: 40, backgroundColor: '#1B3A6B', justifyContent: 'center', alignItems: 'center', marginBottom: 12 },
  avatarTexto: { color: '#FFD700', fontSize: 32, fontWeight: '800' },
  nombre: { fontSize: 20, fontWeight: '700', color: '#1A202C', marginBottom: 4 },
  email: { fontSize: 14, color: '#718096', marginBottom: 10 },
  rolBadge: { backgroundColor: '#E6F4FF', paddingHorizontal: 14, paddingVertical: 5, borderRadius: 20, marginBottom: 8 },
  rolTexto: { fontSize: 13, fontWeight: '700', color: '#1B3A6B' },
  fechaRegistro: { fontSize: 12, color: '#A0AEC0' },
  seccion: {
    backgroundColor: '#FFFFFF', borderRadius: 12, padding: 20, marginBottom: 14,
    shadowColor: '#000', shadowOffset: { width: 0, height: 2 }, shadowOpacity: 0.05, shadowRadius: 6, elevation: 2,
  },
  seccionTitulo: { fontSize: 16, fontWeight: '700', color: '#1B3A6B', marginBottom: 12 },
  fila: { borderTopWidth: 1, borderTopColor: '#EDF2F7', paddingVertical: 12 },
  filaLabel: { fontSize: 12, color: '#718096', marginBottom: 3 },
  filaValor: { fontSize: 15, color: '#1A202C', fontWeight: '600' },
  habilidadesHint: { fontSize: 13, color: '#718096', marginBottom: 14 },
  habilidadesGrid: { flexDirection: 'row', flexWrap: 'wrap', gap: 8, marginBottom: 16 },
  habilidadChip: { paddingHorizontal: 14, paddingVertical: 8, borderRadius: 20, backgroundColor: '#EDF2F7', borderWidth: 1.5, borderColor: '#E2E8F0' },
  habilidadChipActiva: { backgroundColor: '#1B3A6B', borderColor: '#1B3A6B' },
  habilidadChipTexto: { fontSize: 13, color: '#4A5568', fontWeight: '600' },
  habilidadChipTextoActivo: { color: '#FFFFFF' },
  guardarBtn: { backgroundColor: '#1B3A6B', paddingVertical: 12, borderRadius: 10, alignItems: 'center' },
  guardarBtnTexto: { color: '#FFFFFF', fontWeight: '700', fontSize: 15 },
  botonInvitaciones: {
    backgroundColor: '#FFFFFF', borderWidth: 2, borderColor: '#1B3A6B',
    paddingVertical: 14, borderRadius: 12, alignItems: 'center',
  },
  botonInvitacionesTexto: { color: '#1B3A6B', fontWeight: '700', fontSize: 15 },
});
