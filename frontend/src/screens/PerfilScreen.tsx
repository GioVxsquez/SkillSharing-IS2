import React, { useEffect, useState, useCallback } from 'react';
import {
  View, Text, ScrollView, StyleSheet, TouchableOpacity,
  ActivityIndicator, Alert, StatusBar, Image, RefreshControl,
} from 'react-native';
import { api } from '../api/config';

// HU23: Ver perfil propio
// HU22: Ver y gestionar habilidades del perfil
export default function PerfilScreen({ navigation }: any) {
  const [perfil, setPerfil]         = useState<any>(null);
  const [habilidades, setHabilidades] = useState<any[]>([]);
  const [misHabilidades, setMisH]   = useState<any[]>([]);
  const [loading, setLoading]       = useState(true);
  const [guardando, setGuardando]   = useState(false);
  const [refreshing, setRefreshing] = useState(false);

  const cargar = useCallback(async () => {
    try {
      const [rPerfil, rHabs, rMisHabs] = await Promise.all([
        api.get('/usuarios/me'),
        api.get('/habilidades'),
        api.get('/usuarios/me'),
      ]);
      setPerfil(rPerfil.data.data);
      setHabilidades(rHabs.data.data || []);
      setMisH((rPerfil.data.data?.habilidades || []).map((h: any) => h.habilidadId));
    } catch {
      Alert.alert('Error', 'No se pudo cargar el perfil.');
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, []);

  useEffect(() => { cargar(); }, [cargar]);

  const toggleHabilidad = (id: number) => {
    setMisH(prev => prev.includes(id) ? prev.filter(h => h !== id) : [...prev, id]);
  };

  const guardarHabilidades = async () => {
    setGuardando(true);
    try {
      const resp = await api.put('/usuarios/me/habilidades', { habilidadIds: misHabilidades });
      if (resp.data.exito) {
        Alert.alert('¡Guardado!', 'Tus habilidades han sido actualizadas.');
      }
    } catch {
      Alert.alert('Error', 'No se pudieron guardar las habilidades.');
    } finally {
      setGuardando(false);
    }
  };

  if (loading) return <ActivityIndicator style={{ flex: 1 }} size="large" color="#1B3A6B" />;

  return (
    <View style={styles.container}>
      <StatusBar barStyle="light-content" backgroundColor="#1B3A6B" />
      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()}>
          <Text style={styles.backTexto}>← Volver</Text>
        </TouchableOpacity>
        <Text style={styles.headerTitulo}>Mi Perfil</Text>
      </View>

      <ScrollView
        contentContainerStyle={styles.scroll}
        refreshControl={<RefreshControl refreshing={refreshing} onRefresh={() => { setRefreshing(true); cargar(); }} colors={['#1B3A6B']} />}
      >
        {/* Avatar y datos */}
        <View style={styles.avatarCard}>
          <View style={styles.avatar}>
            <Text style={styles.avatarTexto}>{perfil?.nombre?.charAt(0)?.toUpperCase() || '?'}</Text>
          </View>
          <Text style={styles.nombre}>{perfil?.nombre}</Text>
          <Text style={styles.email}>{perfil?.email}</Text>
          <View style={styles.rolBadge}>
            <Text style={styles.rolTexto}>{perfil?.rol === 'INSTRUCTOR' ? '📚 Instructor' : '🎓 Aprendiz'}</Text>
          </View>
          <Text style={styles.fechaRegistro}>
            Miembro desde: {perfil?.fechaRegistro ? new Date(perfil.fechaRegistro).toLocaleDateString('es-PE') : '—'}
          </Text>
        </View>

        {/* Sección habilidades */}
        <View style={styles.seccion}>
          <Text style={styles.seccionTitulo}>Mis Habilidades</Text>
          <Text style={styles.seccionHint}>Toca para agregar o quitar habilidades de tu perfil</Text>

          <View style={styles.habilidadesGrid}>
            {habilidades.map((hab: any) => {
              const seleccionada = misHabilidades.includes(hab.habilidadId);
              return (
                <TouchableOpacity
                  key={hab.habilidadId}
                  style={[styles.habChip, seleccionada && styles.habChipActivo]}
                  onPress={() => toggleHabilidad(hab.habilidadId)}
                >
                  <Text style={[styles.habTexto, seleccionada && styles.habTextoActivo]}>
                    {hab.nombre}
                  </Text>
                </TouchableOpacity>
              );
            })}
          </View>

          <TouchableOpacity
            style={[styles.botonGuardar, guardando && { opacity: 0.7 }]}
            onPress={guardarHabilidades}
            disabled={guardando}
          >
            {guardando
              ? <ActivityIndicator color="#fff" />
              : <Text style={styles.botonTexto}>💾 Guardar Habilidades</Text>}
          </TouchableOpacity>
        </View>

        {/* Botón ver mis invitaciones */}
        <TouchableOpacity style={styles.botonInvitaciones} onPress={() => navigation.navigate('Invitaciones')}>
          <Text style={styles.botonInvitacionesTexto}>📬 Ver mis invitaciones</Text>
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
  avatarCard: { backgroundColor: '#FFFFFF', borderRadius: 16, padding: 24, alignItems: 'center', marginBottom: 16, shadowColor: '#000', shadowOffset: { width: 0, height: 2 }, shadowOpacity: 0.07, shadowRadius: 6, elevation: 3 },
  avatar: { width: 80, height: 80, borderRadius: 40, backgroundColor: '#1B3A6B', justifyContent: 'center', alignItems: 'center', marginBottom: 12 },
  avatarTexto: { color: '#FFD700', fontSize: 32, fontWeight: '800' },
  nombre: { fontSize: 20, fontWeight: '700', color: '#1A202C', marginBottom: 4 },
  email: { fontSize: 14, color: '#718096', marginBottom: 10 },
  rolBadge: { backgroundColor: '#E6F4FF', paddingHorizontal: 14, paddingVertical: 5, borderRadius: 20, marginBottom: 8 },
  rolTexto: { fontSize: 13, fontWeight: '700', color: '#1B3A6B' },
  fechaRegistro: { fontSize: 12, color: '#A0AEC0' },
  seccion: { backgroundColor: '#FFFFFF', borderRadius: 16, padding: 20, marginBottom: 14, shadowColor: '#000', shadowOffset: { width: 0, height: 2 }, shadowOpacity: 0.05, shadowRadius: 6, elevation: 2 },
  seccionTitulo: { fontSize: 16, fontWeight: '700', color: '#1B3A6B', marginBottom: 4 },
  seccionHint: { fontSize: 12, color: '#718096', marginBottom: 16 },
  habilidadesGrid: { flexDirection: 'row', flexWrap: 'wrap', gap: 8, marginBottom: 20 },
  habChip: { paddingHorizontal: 14, paddingVertical: 8, borderRadius: 20, borderWidth: 1.5, borderColor: '#E2E8F0', backgroundColor: '#F7F9FC' },
  habChipActivo: { backgroundColor: '#1B3A6B', borderColor: '#1B3A6B' },
  habTexto: { fontSize: 13, fontWeight: '600', color: '#718096' },
  habTextoActivo: { color: '#FFFFFF' },
  botonGuardar: { backgroundColor: '#1B3A6B', paddingVertical: 14, borderRadius: 10, alignItems: 'center', elevation: 3 },
  botonTexto: { color: '#FFFFFF', fontWeight: '700', fontSize: 15 },
  botonInvitaciones: { backgroundColor: '#FFFFFF', borderWidth: 2, borderColor: '#1B3A6B', paddingVertical: 14, borderRadius: 12, alignItems: 'center' },
  botonInvitacionesTexto: { color: '#1B3A6B', fontWeight: '700', fontSize: 15 },
});
