import React, { useEffect, useState } from 'react';
import {
  View, Text, FlatList, TouchableOpacity, StyleSheet,
  ActivityIndicator, Alert, StatusBar, ScrollView,
} from 'react-native';
import { api } from '../api/config';

// US22
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
      <StatusBar barStyle="light-content" backgroundColor="#0F1C36" />
      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()}>
          <Text style={styles.backTexto}>Volver</Text>
        </TouchableOpacity>
        <Text style={styles.headerTitulo}>Instructores</Text>
        <View style={{ width: 40 }} />
      </View>

      <View style={styles.habSeccion}>
        <Text style={styles.habLabel}>¿Qué quieres aprender?</Text>
        {cargandoHab ? (
          <ActivityIndicator color="#F97316" />
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

      {loading ? (
        <ActivityIndicator size="large" color="#F97316" style={{ marginTop: 40 }} />
      ) : (
        <FlatList
          data={instructores}
          keyExtractor={(item) => String(item.usuarioId)}
          contentContainerStyle={styles.lista}
          renderItem={({ item }) => (
            <View style={styles.card}>
              <View style={styles.avatar}>
                <Text style={styles.avatarTexto}>{item.nombre?.charAt(0)?.toUpperCase() || '?'}</Text>
              </View>
              <View style={styles.cardDatos}>
                <Text style={styles.nombre}>{item.nombre}</Text>
                <Text style={styles.email}>{item.email}</Text>
                {item.reputacionPromedio > 0 ? (
                  <View style={styles.repRow}>
                    <Text style={styles.repEstrellas}>
                      {'★'.repeat(Math.round(item.reputacionPromedio))}{'☆'.repeat(5 - Math.round(item.reputacionPromedio))}
                    </Text>
                    <Text style={styles.repNumero}>{item.reputacionPromedio.toFixed(1)}</Text>
                  </View>
                ) : (
                  <Text style={styles.sinRep}>Sin calificaciones</Text>
                )}
                {item.habilidades?.length > 0 && (
                  <View style={styles.habilidadesRow}>
                    {item.habilidades.slice(0, 3).map((h: string, i: number) => (
                      <View key={i} style={styles.habBadge}>
                        <Text style={styles.habTexto}>{h}</Text>
                      </View>
                    ))}
                  </View>
                )}
              </View>
            </View>
          )}
          ListEmptyComponent={
            <Text style={styles.vacio}>
              {selected ? 'Nadie con esa habilidad por ahora.' : 'Elige una habilidad para ver instructores.'}
            </Text>
          }
        />
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#F0F4F8' },
  header: {
    backgroundColor: '#0F1C36',
    paddingTop: 52, paddingBottom: 20, paddingHorizontal: 20,
    flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center',
  },
  backTexto: { color: '#8898AA', fontSize: 14, fontWeight: '600' },
  headerTitulo: { color: '#FFFFFF', fontSize: 18, fontWeight: '800' },
  habSeccion: {
    backgroundColor: '#FFFFFF', paddingVertical: 16,
    borderBottomWidth: 1, borderBottomColor: '#E8EDF2',
  },
  habLabel: { fontSize: 13, fontWeight: '700', color: '#4A5568', marginBottom: 12, paddingHorizontal: 16 },
  chipsRow: { gap: 8, paddingHorizontal: 16 },
  chip: {
    paddingHorizontal: 14, paddingVertical: 8, borderRadius: 20,
    backgroundColor: '#F0F4F8', borderWidth: 1.5, borderColor: '#E2E8F0',
  },
  chipActivo: { backgroundColor: '#F97316', borderColor: '#F97316' },
  chipTexto: { fontSize: 13, color: '#4A5568', fontWeight: '600' },
  chipTextoActivo: { color: '#FFFFFF', fontWeight: '700' },
  lista: { padding: 16 },
  card: {
    backgroundColor: '#FFFFFF', borderRadius: 16, padding: 16, marginBottom: 12,
    flexDirection: 'row', alignItems: 'flex-start', gap: 14,
    shadowColor: '#0F1C36', shadowOffset: { width: 0, height: 2 }, shadowOpacity: 0.07, shadowRadius: 8, elevation: 3,
  },
  avatar: {
    width: 52, height: 52, borderRadius: 26,
    backgroundColor: '#0F1C36',
    justifyContent: 'center', alignItems: 'center',
    flexShrink: 0,
  },
  avatarTexto: { color: '#F97316', fontSize: 22, fontWeight: '900' },
  cardDatos: { flex: 1 },
  nombre: { fontSize: 15, fontWeight: '800', color: '#0F1C36', marginBottom: 2 },
  email: { fontSize: 13, color: '#8898AA', marginBottom: 6 },
  repRow: { flexDirection: 'row', alignItems: 'center', gap: 6, marginBottom: 6 },
  repEstrellas: { color: '#F59E0B', fontSize: 14, fontWeight: '700' },
  repNumero: { color: '#F59E0B', fontSize: 13, fontWeight: '700' },
  sinRep: { fontSize: 12, color: '#CBD5E0', marginBottom: 6 },
  habilidadesRow: { flexDirection: 'row', flexWrap: 'wrap', gap: 6 },
  habBadge: { backgroundColor: '#FFF0E6', paddingHorizontal: 8, paddingVertical: 3, borderRadius: 12 },
  habTexto: { fontSize: 11, color: '#F97316', fontWeight: '700' },
  vacio: { textAlign: 'center', color: '#8898AA', marginTop: 60, fontSize: 15 },
});
