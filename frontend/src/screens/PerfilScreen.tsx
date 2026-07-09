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
    return (
      <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center', backgroundColor: '#0F1C36' }}>
        <ActivityIndicator size="large" color="#F97316" />
      </View>
    );
  }

  const inicial = perfil?.nombre?.charAt(0)?.toUpperCase() || '?';

  return (
    <View style={styles.container}>
      <StatusBar barStyle="light-content" backgroundColor="#0F1C36" />

      {/* Header con avatar integrado */}
      <View style={styles.hero}>
        <TouchableOpacity onPress={() => navigation.goBack()} style={styles.volverBtn}>
          <Text style={styles.volverTexto}>Volver</Text>
        </TouchableOpacity>
        <View style={styles.avatarWrap}>
          <View style={styles.avatar}>
            <Text style={styles.avatarLetra}>{inicial}</Text>
          </View>
          <View style={styles.rolPill}>
            <Text style={styles.rolPillTexto}>
              {perfil?.rol === 'INSTRUCTOR' ? 'Instructor' : 'Aprendiz'}
            </Text>
          </View>
        </View>
        <Text style={styles.nombre}>{perfil?.nombre}</Text>
        <Text style={styles.email}>{perfil?.email}</Text>
      </View>

      <ScrollView
        contentContainerStyle={styles.scroll}
        refreshControl={
          <RefreshControl refreshing={refreshing} onRefresh={() => { setRefreshing(true); cargar(); }} colors={['#F97316']} />
        }
      >
        {/* Datos de cuenta */}
        <View style={styles.seccion}>
          <Text style={styles.seccionTitulo}>Cuenta</Text>
          {[
            { label: 'Nombre', valor: perfil?.nombre },
            { label: 'Correo', valor: perfil?.email },
            { label: 'Rol', valor: perfil?.rol },
            { label: 'Miembro desde', valor: perfil?.fechaRegistro ? new Date(perfil.fechaRegistro).toLocaleDateString('es-PE') : '-' },
          ].map((fila) => (
            <View key={fila.label} style={styles.fila}>
              <Text style={styles.filaLabel}>{fila.label}</Text>
              <Text style={styles.filaValor}>{fila.valor || '-'}</Text>
            </View>
          ))}
        </View>

        {/* Habilidades */}
        <View style={styles.seccion}>
          <Text style={styles.seccionTitulo}>Mis habilidades</Text>
          <Text style={styles.hint}>Toca las que dominas para que otros te encuentren</Text>
          <View style={styles.grid}>
            {habilidades.map((h) => {
              const activa = misHabilidades.includes(h.habilidadId);
              return (
                <TouchableOpacity
                  key={h.habilidadId}
                  style={[styles.chip, activa && styles.chipActivo]}
                  onPress={() => toggleHabilidad(h.habilidadId)}
                >
                  <Text style={[styles.chipTexto, activa && styles.chipTextoActivo]}>
                    {activa ? '✓ ' : ''}{h.nombre}
                  </Text>
                </TouchableOpacity>
              );
            })}
          </View>
          <TouchableOpacity
            style={[styles.boton, guardando && { opacity: 0.7 }]}
            onPress={guardarHabilidades}
            disabled={guardando}
          >
            {guardando
              ? <ActivityIndicator color="#fff" size="small" />
              : <Text style={styles.botonTexto}>Guardar habilidades</Text>}
          </TouchableOpacity>
        </View>

        {/* Accesos rápidos */}
        <View style={styles.accionesRow}>
          <TouchableOpacity style={styles.accionBtn} onPress={() => navigation.navigate('Invitaciones')}>
            <Text style={styles.accionBtnTexto}>Mis invitaciones</Text>
          </TouchableOpacity>
          <TouchableOpacity style={[styles.accionBtn, styles.accionBtnSecundario]} onPress={() => navigation.navigate('Notificaciones')}>
            <Text style={[styles.accionBtnTexto, { color: '#F97316' }]}>Notificaciones</Text>
          </TouchableOpacity>
        </View>

      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#F0F4F8' },
  hero: {
    backgroundColor: '#0F1C36',
    paddingTop: 52,
    paddingBottom: 32,
    alignItems: 'center',
  },
  volverBtn: { alignSelf: 'flex-start', marginLeft: 20, marginBottom: 20 },
  volverTexto: { color: '#8898AA', fontSize: 14, fontWeight: '600' },
  avatarWrap: { alignItems: 'center', marginBottom: 12 },
  avatar: {
    width: 80, height: 80, borderRadius: 40,
    backgroundColor: '#F97316',
    justifyContent: 'center', alignItems: 'center',
    borderWidth: 3, borderColor: '#1A2E50',
    shadowColor: '#F97316', shadowOffset: { width: 0, height: 6 }, shadowOpacity: 0.4, shadowRadius: 12, elevation: 8,
  },
  avatarLetra: { color: '#FFFFFF', fontSize: 34, fontWeight: '900' },
  rolPill: {
    marginTop: 8,
    backgroundColor: '#1A2E50',
    paddingHorizontal: 14, paddingVertical: 4,
    borderRadius: 20,
  },
  rolPillTexto: { color: '#F97316', fontSize: 12, fontWeight: '800' },
  nombre: { color: '#FFFFFF', fontSize: 20, fontWeight: '800', marginBottom: 4 },
  email: { color: '#8898AA', fontSize: 13 },
  scroll: { padding: 16, paddingBottom: 40 },
  seccion: {
    backgroundColor: '#FFFFFF', borderRadius: 16, padding: 20,
    marginBottom: 14,
    shadowColor: '#0F1C36', shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.06, shadowRadius: 8, elevation: 2,
  },
  seccionTitulo: { fontSize: 15, fontWeight: '800', color: '#0F1C36', marginBottom: 14 },
  fila: { borderTopWidth: 1, borderTopColor: '#F0F4F8', paddingVertical: 12 },
  filaLabel: { fontSize: 11, color: '#8898AA', fontWeight: '700', textTransform: 'uppercase', letterSpacing: 0.5, marginBottom: 3 },
  filaValor: { fontSize: 14, color: '#0F1C36', fontWeight: '600' },
  hint: { fontSize: 13, color: '#8898AA', marginBottom: 14 },
  grid: { flexDirection: 'row', flexWrap: 'wrap', gap: 8, marginBottom: 16 },
  chip: {
    paddingHorizontal: 14, paddingVertical: 8, borderRadius: 20,
    backgroundColor: '#F0F4F8', borderWidth: 1.5, borderColor: '#E2E8F0',
  },
  chipActivo: { backgroundColor: '#0F1C36', borderColor: '#0F1C36' },
  chipTexto: { fontSize: 13, color: '#4A5568', fontWeight: '600' },
  chipTextoActivo: { color: '#FFFFFF' },
  boton: {
    backgroundColor: '#F97316', paddingVertical: 13, borderRadius: 12,
    alignItems: 'center',
    shadowColor: '#F97316', shadowOffset: { width: 0, height: 4 }, shadowOpacity: 0.3, shadowRadius: 8, elevation: 4,
  },
  botonTexto: { color: '#FFFFFF', fontWeight: '800', fontSize: 14 },
  accionesRow: { flexDirection: 'row', gap: 12 },
  accionBtn: {
    flex: 1, paddingVertical: 14, borderRadius: 12,
    alignItems: 'center',
    backgroundColor: '#0F1C36',
  },
  accionBtnSecundario: {
    backgroundColor: '#FFF0E6',
    borderWidth: 1.5, borderColor: '#F97316',
  },
  accionBtnTexto: { color: '#FFFFFF', fontWeight: '700', fontSize: 14 },
});
