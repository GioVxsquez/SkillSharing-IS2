import React, { useEffect, useState, useCallback } from 'react';
import {
  View, Text, FlatList, TextInput, TouchableOpacity, StyleSheet,
  ActivityIndicator, Alert, StatusBar, ScrollView, RefreshControl,
} from 'react-native';
import { api } from '../api/config';

// US22: Buscar instructores por habilidad
export default function BuscarInstructoresScreen({ navigation }: any) {
  const [habilidades, setHabilidades]   = useState<any[]>([]);
  const [instructores, setInstructores] = useState<any[]>([]);
  const [selected, setSelected]         = useState<string | null>(null);
  const [loading, setLoading]           = useState(false);
  const [cargandoHab, setCargandoHab]   = useState(true);

  useEffect(() => {
    api.get('/habilidades')
      .then((r) => setHabilidades(r.data.data || []))
      .catch(() => Alert.alert('Error', 'No se pudieron cargar las habilidades.'))
      .finally(() => setCargandoHab(false));
  }, []);

  const buscarInstructores = async (habilidad: string) => {
    setSelected(habilidad);
    setLoading(true);
    try {
      const resp = await api.get(`/habilidades/instructores?habilidad=${habilidad}`);
      setInstructores(resp.data.data || []);
    } catch {
      Alert.alert('Error', 'No se pudieron cargar los instructores.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <View style={styles.container}>
      <StatusBar barStyle="light-content" backgroundColor="#1B3A6B" />
      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()}>
          <Text style={styles.backTexto}>← Volver</Text>
        </TouchableOpacity>
        <Text style={styles.headerTitulo}>Buscar Instructores</Text>
      </View>

      {/* Selector de habilidad */}
      <View style={styles.seccionHab}>
        <Text style={styles.label}>Selecciona una habilidad:</Text>
        {cargandoHab ? (
          <ActivityIndicator color="#1B3A6B" />
        ) : (
          <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={styles.chipsRow}>
            {habilidades.map((h) => (
              <TouchableOpacity
                key={h.habilidadId}
                style={[styles.chip, selected === h.nombre && styles.chipActivo]}
                onPress={() => buscarInstructores(h.nombre)}
              >
                <Text style={[styles.chipTexto, selected === h.nombre && styles.chipTextoActivo]}>
                  {h.nombre}
                </Text>
              </TouchableOpacity>
            ))}
          </ScrollView>
        )}
      </View>

      {/* Resultados */}
      {loading ? (
        <ActivityIndicator size="large" color="#1B3A6B" style={{ marginTop: 40 }} />
      ) : (
        <FlatList
          data={instructores}
          keyExtractor={(item) => String(item.usuarioId)}
          contentContainerStyle={styles.lista}
          renderItem={({ item }) => (
            <View style={styles.card}>
              <View style={styles.cardRow}>
                <View style={styles.avatar}>
                  <Text style={styles.avatarTexto}>{item.nombre?.charAt(0)?.toUpperCase() || '?'}</Text>
                </View>
                <View style={{ flex: 1 }}>
                  <Text style={styles.nombre}>{item.nombre}</Text>
                  <Text style={styles.email}>{item.email}</Text>
                  {item.habilidades?.length > 0 && (
                    <View style={styles.habilidadesRow}>
                      {item.habilidades.slice(0, 3).map((h: any) => (
                        <View key={h.habilidadId} style={styles.habBadge}>
                          <Text style={styles.habTexto}>{h.nombre}</Text>
                        </View>
                      ))}
                    </View>
                  )}
                </View>
              </View>
            </View>
          )}
          ListEmptyComponent={
            selected ? (
              <Text style={styles.vacio}>No hay instructores con esta habilidad.</Text>
            ) : (
              <Text style={styles.vacio}>Selecciona una habilidad para buscar instructores.</Text>
            )
          }
        />
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#F7F9FC' },
  header: { backgroundColor: '#1B3A6B', paddingTop: 48, paddingBottom: 16, paddingHorizontal: 16 },
  backTexto: { color: '#C8D8F0', fontSize: 14, marginBottom: 6, fontWeight: '600' },
  headerTitulo: { color: '#FFFFFF', fontSize: 18, fontWeight: '700' },
  seccionHab: { backgroundColor: '#FFFFFF', padding: 16, borderBottomWidth: 1, borderBottomColor: '#E2E8F0' },
  label: { fontSize: 13, fontWeight: '700', color: '#4A5568', marginBottom: 10 },
  chipsRow: { gap: 8, paddingRight: 16 },
  chip: { paddingHorizontal: 14, paddingVertical: 8, borderRadius: 20, backgroundColor: '#EDF2F7', borderWidth: 1.5, borderColor: '#E2E8F0' },
  chipActivo: { backgroundColor: '#1B3A6B', borderColor: '#1B3A6B' },
  chipTexto: { fontSize: 13, color: '#4A5568', fontWeight: '600' },
  chipTextoActivo: { color: '#FFFFFF' },
  lista: { padding: 16 },
  card: { backgroundColor: '#FFFFFF', borderRadius: 14, padding: 16, marginBottom: 12, shadowColor: '#000', shadowOffset: { width: 0, height: 2 }, shadowOpacity: 0.06, shadowRadius: 6, elevation: 2 },
  cardRow: { flexDirection: 'row', alignItems: 'center', gap: 12 },
  avatar: { width: 48, height: 48, borderRadius: 24, backgroundColor: '#1B3A6B', justifyContent: 'center', alignItems: 'center' },
  avatarTexto: { color: '#FFD700', fontSize: 20, fontWeight: '800' },
  nombre: { fontSize: 15, fontWeight: '700', color: '#1A202C' },
  email: { fontSize: 13, color: '#718096', marginTop: 2 },
  habilidadesRow: { flexDirection: 'row', flexWrap: 'wrap', gap: 6, marginTop: 8 },
  habBadge: { backgroundColor: '#EBF4FF', paddingHorizontal: 8, paddingVertical: 3, borderRadius: 12 },
  habTexto: { fontSize: 11, color: '#1B3A6B', fontWeight: '600' },
  vacio: { textAlign: 'center', color: '#718096', marginTop: 60, fontSize: 15 },
});
